var app = require("./app");
var r = require("rethinkdb");

function sendStats(ws, type, stats) {
  try {
    ws.send(JSON.stringify({type: type, data: stats}));
  } catch (e) {
    console.log(e);
  }
}

function createResultsHandler(ws, type) {
  return function(err, cursor) {
    if (err) {
      console.log(err);
    } else {
      cursor.each(function(err, row) {
        if (err) {
          console.log(err);
        } else {
          sendStats(ws, type, row.new_val);
        }
      });
    }
  }
}

// Define websocket endpoint:
app.ws("/ws/stats/:appId", function(ws, req) {

  // Connect to RethinkDB, and listen to various changes:
  r.connect({db: "af", host: "localhost", port: 28015}, function(err, conn) {
    if (err) {
      console.log(err);
    } else {
      r.table("geo_stats").filter({"appId": req.params.appId})
        .changes({includeInitial: true}).run(conn, createResultsHandler(ws, "geo"));

      r.table("total_stats").filter({"appId": req.params.appId})
        .changes({includeInitial: true}).run(conn, createResultsHandler(ws, "total"));

      r.table("device_stats").filter({"appId": req.params.appId})
        .changes({includeInitial: true}).run(conn, createResultsHandler(ws, "devices"));
    }
  });
});

