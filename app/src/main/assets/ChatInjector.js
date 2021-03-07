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
      console.error('Failed to dispatch data', e);
    }
  }
  return result;
}

// Process YouTube chat data
window.addEventListener('messageReceive', d => messageReceiveCallback(d.detail));

// Send processed data back to app
window.addEventListener('messagePostProcess', d => window.Android.receiveMessages(d.detail))

const isReplay = window.location.href.startsWith('https://www.youtube.com/live_chat_replay');

const getUsec = (timestamp, usec) => {
  let secs = Array.from(timestamp.split(':'), t => parseInt(t, 10)).reverse();
  secs = secs[0] + (secs[1] ? secs[1] * 60 : 0) + (secs[2] ? secs[2] * 60 * 60 : 0);
  secs *= 1000;
  secs += usec % 1000;
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
    if (!response.continuationContents) {
      console.warn('Response was invalid', JSON.stringify(response));
      return;
    }

    const messages = [];
    const date = new Date();
    (response.continuationContents.liveChatContinuation.actions || []).forEach((action, i) => {
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

        let isAuthorModerator = false;
        let authorMembership = null;
        (messageItem.authorBadges || []).forEach((badge) => {
          if (badge.liveChatAuthorBadgeRenderer.tooltip === 'Moderator') {
            isAuthorModerator = true;
          }

          if (badge.liveChatAuthorBadgeRenderer.tooltip.startsWith('Member')) {
            const thumbnails = badge.liveChatAuthorBadgeRenderer.customThumbnail.thumbnails;
            authorMembership = {
              name: badge.liveChatAuthorBadgeRenderer.tooltip,
              thumbnailSrc: thumbnails[thumbnails.length - 1].url
            }
          }
        });

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
              const thumbnails = run.emoji.image.thumbnails;
              runs.push({
                type: 'emoji',
                emojiId: run.emoji.shortcuts[0],
                emojiSrc: thumbnails[thumbnails.length - 1].url
              });
            }
          });
        }

        const timestampUsec = parseInt(messageItem.timestampUsec, 10);
        const authorThumbnails = messageItem.authorPhoto.thumbnails;
        const item = {
          author: {
            name: messageItem.authorName.simpleText,
            id: messageItem.authorExternalChannelId,
            photo: authorThumbnails[authorThumbnails.length - 1].url,
            isModerator: isAuthorModerator,
            membershipBadge: authorMembership,
          },
          index: i,
          messages: runs,
          timestamp: timestampUsec,
          delay: isReplay
            ? getUsec(messageItem.timestampText.simpleText, timestampUsec)
            : (date.getTime() * 1000) - timestampUsec
        };

        if (currentElement.liveChatPaidMessageRenderer) {
          item.superchat = {
            amount: messageItem.purchaseAmountText.simpleText,
            color: colorConversionTable[messageItem.bodyBackgroundColor]
          };
        }

        messages.push(item);
      } catch (e) {
        console.error('Error while parsing message.', e);
      }
    });

    window.dispatchEvent(new CustomEvent('messagePostProcess', {
      detail: JSON.stringify({ messages, isReplay })
    }));
  } catch (e) {
    console.debug(e);
  }
};