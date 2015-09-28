/*转换为Confirm*/
function invokeVerify(data) {
  var msg = new Message(data);

  var _yk = new Object;
  _yk[data.action] = function(args) {
    msg.args = args;
    return confirm(msg.msg_content);
  };

  Object.defineProperty(msg.obj, data.action, {
    value: function(args) {
      return _yk[data.action](args);
    }
  });
}
