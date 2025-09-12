# Flipper

Tutorial on installing the Flipper application on a computer: https://fbflipper.com/docs/getting-started/

## Add custom plugins

For the plugin in the `flipper-plugin-demeter-tracer` folder, run the command `yarn install && yarn build` in that folder.
To install `yarn` run the command `brew install yarn`.

Check `~/.flipper/config.json` and add parent path of your plugin to the `pluginPaths` array in the config.

Now connect your phone, launch Flipper and enable built plugins. You're all set!
