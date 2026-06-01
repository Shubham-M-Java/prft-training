const PROXY_CONFIG = [{
  context: ['/api'],
  target: 'http://localhost:8090',
  secure: false,
  changeOrigin: true,
  onProxyRes: function(proxyRes) {
    delete proxyRes.headers['www-authenticate'];
  }
}];

module.exports = PROXY_CONFIG;
