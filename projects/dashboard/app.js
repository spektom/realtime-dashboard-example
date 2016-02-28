var express = require("express");
var path = require("path");
var app = express();
var expressWs = require("express-ws")(app);

module.exports = app

app.set("views", path.join(__dirname, "views"));
app.set("view engine", "jade");
app.use(express.static(path.join(__dirname, "public")));

app.get("/", function(req, res) {
  res.render("index", { title: "Real-Time Dashboard" });
});

require("./ws")

app.set("port", process.env.PORT || 3000);
app.listen(app.get("port"));

