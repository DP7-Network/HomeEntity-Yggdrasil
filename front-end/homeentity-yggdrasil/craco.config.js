const SimpleProgressWebpackPlugin = require('simple-progress-webpack-plugin');
const path = require("path");


module.exports = {
  style: {
    postcss: {
      plugins: [require("autoprefixer")],
    },
  },
  webpack: {
    alias: {
      "#": path.resolve("src"),
    },
    plugins: [
      new SimpleProgressWebpackPlugin()
    ]
  }
};
