for (event_name of ['visibilitychange', 'webkitvisibilitychange', 'blur']) {
  window.addEventListener(event_name, e => e.stopImmediatePropagation(), true);
}

window.fetchFallback = window.fetch;
window.fetch = async (...args) => {
  const url = args[0].url;
  const result = await window.fetchFallback(...args);

  if (url.startsWith('https://www.youtube.com/youtubei/v1/live_chat/get_live_chat')) {
    const response = await (await result.clone()).json();
    try {
      window.dispatchEvent(new CustomEvent('messageReceive', {
        detail: response
      }));
    } catch (e) {
      console.log(e);
    }
  }
  return result;
}

window.addEventListener('messageReceive', d => messageReceiveCallback(d.detail));
window.addEventListener('messagePostProcess', d => window.Android.receiveMessages(d.detail))

const isReplay = window.location.href.startsWith(
  'https://www.youtube.com/live_chat_replay'
);

const formatTimestamp = (timestamp) => {
  return (new Date(parseInt(timestamp) / 1000)).toLocaleTimeString(navigator.language, {
    hour: '2-digit',
    minute: '2-digit'
  });
};

const getMillis = (timestamp, usec) => {
  let secs = Array.from(timestamp.split(':'), t => parseInt(t)).reverse();
  secs = secs[0] + (secs[1] ? secs[1] * 60 : 0) + (secs[2] ? secs[2] * 60 * 60 : 0);
  secs *= 1000;
  secs += usec % 1000;
  secs /= 1000;
  return secs;
};

const colorConversionTable = {
  4280191205: 'BLUE',
  4278248959: 'LIGHT_BLUE',
  4280150454: 'TURQUOISE',
  4294953512: 'YELLOW',
  4294278144: 'ORANGE',
  4293467747: 'PINK',
  4293271831: 'RED'
};

const messageReceiveCallback = async (response) => {
  try {
    const messages = [];
    if (!response.continuationContents) {
      console.debug('Response was invalid', response);
      return;
    }
    (
      response.continuationContents.liveChatContinuation.actions || []
    ).forEach((action, i) => {
      try {
        let currentElement = action.addChatItemAction;
        if (action.replayChatItemAction != null) {
          const thisAction = action.replayChatItemAction.actions[0];
          currentElement = thisAction.addChatItemAction;
        }
        currentElement = (currentElement || {}).item;
        if (!currentElement) {
          return;
        }
        const messageItem = currentElement.liveChatTextMessageRenderer ||
          currentElement.liveChatPaidMessageRenderer ||
          currentElement.liveChatPaidStickerRenderer;
        if (!messageItem) {
          return;
        }
        if (!messageItem.authorName) {
          console.log(currentElement);
          return;
        }
        messageItem.authorBadges = messageItem.authorBadges || [];
        const authorTypes = [];
        messageItem.authorBadges.forEach((badge) =>
          authorTypes.push(badge.liveChatAuthorBadgeRenderer.tooltip.toLowerCase())
        );
        const runs = [];
        if (messageItem.message) {
          messageItem.message.runs.forEach((run) => {
            if (run.text) {
              runs.push({
                type: 'text',
                text: decodeURIComponent(escape(unescape(encodeURIComponent(
                  run.text
                ))))
              });
            } else if (run.emoji) {
              runs.push({
                type: 'emote',
                emoteId: run.emoji.shortcuts[0],
                emoteSrc: run.emoji.image.thumbnails[0].url
              });
            }
          });
        }
        const timestampUsec = parseInt(messageItem.timestampUsec);
        const timestampText = (messageItem.timestampText || {}).simpleText;
        const date = new Date();
        const authorThumbnails = messageItem.authorPhoto.thumbnails;
        const item = {
          author: {
            name: messageItem.authorName.simpleText,
            id: messageItem.authorExternalChannelId,
            photo: authorThumbnails[authorThumbnails.length - 1].url,
            types: authorTypes
          },
          index: i,
          messages: runs,
          timestamp: Math.round(parseInt(timestampUsec) / 1000),
          showtime: isReplay ? getMillis(timestampText, timestampUsec) :
            date.getTime() - Math.round(timestampUsec / 1000)
        };
        if (currentElement.liveChatPaidMessageRenderer) {
          item.superchat = {
            amount: messageItem.purchaseAmountText.simpleText,
            color: colorConversionTable[messageItem.bodyBackgroundColor]
          };
        }
        messages.push(item);
      } catch (e) {
        console.debug('Error while parsing message.', {
          e
        });
      }
    });
    const chunk = {
      messages: messages,
      isReplay
    };

    window.dispatchEvent(new CustomEvent('messagePostProcess', {
      detail: JSON.stringify(chunk)
    }));
  } catch (e) {
    console.debug(e);
  }
};