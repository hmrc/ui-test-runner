import * as axe from 'axe-core';

window.onload = async () => {
  const axeResults = await getAxeResults();
  chrome.runtime.sendMessage({axeResults});
};

async function getAxeResults() {
  const axeOptions: axe.RunOptions = {
    runOnly: {
      type: 'tag',
      values: [
        'best-practice',
        'wcag2a',
        'wcag2aa',
        'wcag21a',
        'wcag21aa',
        'wcag22aa',
      ],
    },
  };

  return await new Promise((resolve) => {
    axe.run(axeOptions, (err, results) => {
      if (err) throw err;
      resolve(results);
    });
  });
}
