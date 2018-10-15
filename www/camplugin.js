// Empty constructor
function CamPlugin() {}

// The function that passes work along to native shells
// Message is a string, duration may be 'long' or 'short'
CamPlugin.prototype.show = function(message, duration, successCallback, errorCallback) {
  var options = {};
  options.message = message;
  options.duration = duration;
  cordova.exec(successCallback, errorCallback, 'CamPlugin', 'show', [options]);
}

// Installation constructor that binds ToastyPlugin to window
CamPlugin.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.camPlugin = new CamPlugin();
  return window.plugins.camPlugin;
};
cordova.addConstructor(CamPlugin.install);
