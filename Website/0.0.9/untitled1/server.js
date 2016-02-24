var express = require('express');
var path = require('path');
var favicon = require('serve-favicon');
var logger = require('morgan');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');


var routes = require('./routes/index');

var app = express();

//socket io callen
app.io = require('socket.io')();

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');

// uncomment after placing your favicon in /public
//app.use(favicon(path.join(__dirname, 'public', 'favicon.ico')));
app.use(logger('dev'));
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: false}));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));

app.use('/', routes);

// catch 404 and forward to error handler
app.use(function (req, res, next) {
    var err = new Error('Not Found');
    err.status = 404;
    next(err);
});

//amqp receive
var amqpreceive = require('amqplib/callback_api');

amqpreceive.connect('amqp://groep3:abc123!@137.135.132.202', function (err, conn) {
    conn.createChannel(function (err, ch) {
        var ex = 'logs';

        ch.assertExchange(ex, 'fanout', {durable: false});

        ch.assertQueue('', {exclusive: true}, function (err, q) {
            // console.log(" [*] Waiting for messages in %s. To exit press CTRL+C", q.queue);
            ch.bindQueue(q.queue, ex, '');

            ch.consume(q.queue, function (msg) {
                //console.log(" [x] %s", msg.content.toString());
                //socket io
                var string = msg.content.toString();
                var array = string.split("/");
                if (array[0] == "1") {
                    app.io.emit('verbruik', string);
                }
            }, {noAck: true});
        });
    });
});

// amqp send
var amqpsend = require('amqplib');
var when = require('when');

app.io.on('connection', function (socket) {
    socket.on('send', function (msg) {
        console.log(msg);

        var key = "1";
        var message = key + "/" + msg;

        console.log(message);

        amqpsend.connect('amqp://groep3:abc123!@137.135.132.202').then(function (conn) {
            return when(conn.createChannel().then(function (ch) {
                var ex = 'huis';
                var ok = ch.assertExchange(ex, 'topic', {durable: false});
                return ok.then(function () {
                    ch.publish(ex, key, new Buffer(message));
                    console.log(" [x] Sent %s:'%s'", key, message);
                    return ch.close();
                });
            })).ensure(function () {
                conn.close();
            })
        }).then(null, console.log);
    })
})


//database connectie
var Connection = require('tedious').Connection;
var config = {
    userName: 'userread',
    password: 'VMware123!',
    server: 'sqlgroep3.database.windows.net',
    // If you are on Microsoft Azure, you need this:
    options: {encrypt: true, database: 'SQLGroep3'}
};
var connection = new Connection(config);
connection.on('connect', function(err) {
    // If no error, then good to proceed.
    console.log("Connected");
    executeStatement();
});

var Request = require('tedious').Request;
var TYPES = require('tedious').TYPES;

function executeStatement() {
    request = new Request("SELECT TOP 10 [UserID],[Achternaam],[Voornaam],[Adres],[Gemeente],[Land],[HuisID],[Passwd],[Email] FROM [dbo].[Users]", function(err) {
        if (err) {
            console.log(err);}
    });
    var result = "";
    request.on('row', function(columns) {
        columns.forEach(function(column) {
            if (column.value === null) {
                console.log('NULL');
            } else {
                result+= column.value + " ";
            }
        });
        console.log(result);
        result ="";
    });

    request.on('done', function(rowCount, more) {
        console.log(rowCount + ' rows returned');
    });
    connection.execSql(request);
}

// error handlers

// development error handler
// will print stacktrace
if (app.get('env') === 'development') {
    app.use(function (err, req, res, next) {
        res.status(err.status || 500);
        res.render('error', {
            message: err.message,
            error: err
        });
    });
}

// production error handler
// no stacktraces leaked to user
app.use(function (err, req, res, next) {
    res.status(err.status || 500);
    res.render('error', {
        message: err.message,
        error: {}
    });
});


module.exports = app;
