const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({

  transpileDependencies: true,
  devServer: {
    proxy: {
      '/api': {         
        target: 'http://localhost:1233',
        changeOrigin: true,
        // pathRewrite: {
        //   '^/api': ''
        // }
      }
    },
    client: {
      overlay: false
    }
  }
})
