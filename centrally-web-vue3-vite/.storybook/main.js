/** @type { import('@storybook/vue3-vite').StorybookConfig } */
const config = {
  // commons 폴더만 지정하고 싶다면 ↓
  stories: [
    "../src/components/common/**/*.stories.@(js|mdx)"
  ],

  // 여러 위치를 한꺼번에 지정할 수도 있습니다.
  // stories: [
  //   "../src/components/commons/**/*.stories.@(js|mdx)",
  //   "../src/widgets/**/*.stories.@(js|mdx)"
  // ],

  // 9.x부터는 필수 애드온이 코어에 내장 → addons 배열 없어도 됩니다.
  framework: "@storybook/vue3-vite",
};
export default config;
