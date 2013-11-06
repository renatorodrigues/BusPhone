
/**
 * Module dependencies.
 */

var express = require('express');
//var routes = require('./routes');
var http = require('http');
var path = require('path');
var auth = require("./routes/auth");
var store = require("./routes/store");
var info = require("./routes/info");
var validation = require("./routes/validation");
var admin = require("./routes/admin");

var app = express();

// all environments
app.set('port', process.env.PORT || 3000);
app.set('views', __dirname + '/views');
app.set('view engine', 'ejs');
app.use(express.favicon());
app.use(express.logger('dev'));
app.use(express.bodyParser());
app.use(express.methodOverride());
app.use(app.router);
app.use(express.static(path.join(__dirname, 'public')));

// development only
if ('development' == app.get('env')) {
  app.use(express.errorHandler());
}

var sqlite3 = require('sqlite3').verbose();
var db = new sqlite3.Database('db.db');

var attachDB = function(req, res, next) {
    req.db = db;
    next();
};

app.post('/login', attachDB, auth.login);
app.post("/register", attachDB, auth.register);
app.post("/bus_login", attachDB, auth.bus_login);
app.post("/bus_register", attachDB, auth.bus_register);

app.get("/admin/users", attachDB, admin.users);
app.get("/admin/bus", attachDB, admin.bus);
app.get("/admin/tickets", attachDB, admin.tickets);
app.get("/admin/clear", attachDB, admin.clear);

app.get("/info", attachDB, info.info);
app.get("/tickets", attachDB, info.tickets);

app.post("/buy", attachDB, store.buy);
app.post("/validate", attachDB, validation.validate);
app.get("/inspect", attachDB, validation.inspect);

//app.get('/register', auth.register);

http.createServer(app).listen(app.get('port'), function(){
  console.log('Express server listening on port ' + app.get('port'));
});
