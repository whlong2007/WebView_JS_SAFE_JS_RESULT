/*转换为Prompt*/
function invokeExecute(data) {
  var msg = new Message(data);

  var _yk = new Object;

  _yk[data.action] = function(args) {
    msg.args = args;

    var result;
    /*   var resultStr = prompt(msg.msg_content, msg.msg_content);*/
    var resultStr = prompt(msg.msg_content);

    if (resultStr) {
      try {
        result = JSON.parse(resultStr);
      } catch (e) {
        console.error("function " + data.action +
          " returns a invalid result. please use json data.\n" + resultStr);
      }
    }

    return result;
  };

  Object.defineProperty(msg.obj, data.action, {
    value: function() {
      return _yk[data.action](arguments);
    }
  });
}
