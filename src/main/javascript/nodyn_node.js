load('jvm-npm.js');

System = java.lang.System;
System.setProperty("java.net.preferIPv4Stack", "true");
System.setProperty("java.net.preferIPv6Addresses", "false");


Nodyn  = io.nodyn;
nodyn  = {};
global = this;

__filename = (typeof __filename === 'undefined') ?
              'node.js' : __filename;
__dirname  = (typeof __dirname === 'undefined') ?
              java.lang.System.getProperty('user.dir') : __dirname;

console    = require('console');
Buffer     = require('buffer').Buffer;
SlowBuffer = Buffer.SlowBuffer;

// Stdout and Stderr
stderr = java.lang.System.err;
stdout = java.lang.System.out;

(function() {
  var Process = require('process');
  process = new Process();

  var streams = require('nodyn/streams');
  var tty     = require('tty');

  Object.defineProperty( process, "terminal", {
    get: function() {
      if ( ! System.console() ) {
        return;
      }
      if ( ! this._terminal )  {
        this._terminal = new io.nodyn.tty.POSIXTerminalWrap( System.in, System.out );
      }
      return this._terminal;
    },
    enumerable: false,
  });

  Object.defineProperty( process, 'stdin', {
    get: function() {
      if ( ! this._stdin ) {
        if ( this.terminal ) {
          this._stdin = new tty.ReadStream( this.terminal );
        } else {
          this._stdin = new streams.InputStream( System.in );
        }
        this._stdin._start();
        this._stdin._stream.readStop();
      }
      return this._stdin;
    }
  });

  Object.defineProperty( process, 'stdout', {
    get: function() {
      if (!this._stdout) {
        if ( this.terminal ) {
          this._stdout = new tty.WriteStream( this.terminal );
        } else {
          this._stdout = new streams.OutputStream( System.out );
        }
        this._stdout._start();
      }
      return this._stdout;
    }
  });

  var timers = require('timers');
  setTimeout = timers.setTimeout;
  clearTimeout   = timers.clearTimeout;
  setInterval    = timers.setInterval;
  clearInterval  = timers.clearTimeout;
  setImmediate   = timers.setImmediate;
  clearImmediate = timers.clearImmediate;

  if (process.argv[1]) {
    var Module = require('module');
    var path = require('path');
    var main = path.resolve(process.argv[1]);
    try {
      Module.runMain(main);
    } catch(e) {
      print(e)
      throw new Error(["Unlucky! Cannot run", process.argv[1]].join(' '));
    }
  }

})();