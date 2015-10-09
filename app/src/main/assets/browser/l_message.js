/*Message*/
function Message(data) {
  var _obj;
  var _obj_name;
  var _action;
  var _args;
  var _timestamp; /*用来回调时候的识别*/
  var _content;

  /**************************************************************************/
  _obj_name = data.obj;
  _obj = getObject(_obj_name);

  if (typeof data.action == "string") {
    _action = data.action;
  }

  _args = data.args;
  /**************************************************************************/

  Object.defineProperty(this, "args", {
    set: function(args) {
      _args = window.undefined;
      _content = window.undefined;

      if (typeof args == "undefined") {
        args = new Object;
      } else if (typeof args != "object") {
        throw "Args should be a json Object! \nat " + _obj_name + "." + _action + "()";
      }

      _args = args;
      /*callback 注册到 window下,名称为obj.action.timestamp*/
      if (_obj && _action && typeof args.callback === "function") {
        _timestamp = new Date().getTime();
        var eventName = _obj_name + "." + _action + "#" + _timestamp;

        var callback = function(event) {
          removeCallback(eventName, arguments.callee);
          args.callback(event.detail);
        };

        addCallback(eventName, callback);
      }
    }
  });

  Object.defineProperty(this, "msg_content", {
    get: function() {
      if (!_content) {
        _content = new Object;

        var tag = Message.prototype.tag;
        if (!tag) {
          throw "The tag of Message is not defined!";
        }

        if (typeof _args != "object") {
          _args = new Object;
        }

        _content.args = _args;
        _content.obj = _obj_name;
        _content.action = _action;
        _content.timestamp = _timestamp;
        _content.tag = tag;
      }

      return JSON.stringify(_content);
    }
  });

  Object.defineProperty(this, "obj", {
    get: function() {
      return _obj;
    }
  });

  function getObject(name) {
    var nameArray = name.split(".");
    var obj = window;
    nameArray.forEach(function(element) {
      if (typeof element !== "string") {
        throw "the name of Object is invalid!";
      }

      if (!obj[element]) {
        obj[element] = new Object;
      }
      obj = obj[element];
    });

    return obj && obj !== window ? obj : window.undefined;
  }
}
