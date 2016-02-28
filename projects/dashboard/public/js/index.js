$(function() {
  function connect(appId) {
    var socket = new WebSocket("ws://" + window.location.hostname + ":3000/ws/stats/" + appId);

    socket.onmessage = function(msg) {
      var stats = JSON.parse(msg.data)
      var data = stats.data

      if (stats.type == "geo") {
        updateMap(data);
      }
      else if (stats.type == "total") {
        updateTotals(data);
      }
      else if (stats.type == "devices") {
        updateDevices(data);
      }
    }

    socket.onclose = function() {
      console.log("Websocket connection lost... reconnecting in 5s");
      setTimeout(function() { connect(appId) }, 5000);
    }
  }

  function updateMap(mapStats) {
    mapdata = []
    stats = mapStats.stats
    for (var k in stats) {
      mapdata.push({code: k, z: stats[k]})
    }
    var map = $("#map").highcharts();
    map.series[1].setData(mapdata);
    map.redraw();
  }

  function updateDevices(deviceStats) {
    devicedata = []
    stats = deviceStats.stats
    for (var k in stats) {
      devicedata.push({name: k, y: stats[k]})
    }
    var devices = $("#devices").highcharts();
    devices.series[0].setData(devicedata);
    devices.redraw();
  }

  function updateTotals(totalStats) {
    $("#install-count").text(totalStats.stats.installs);
  }

  var mapData = Highcharts.geojson(Highcharts.maps["custom/world"]);

  $("#map").highcharts("Map", {
    title: { text: "" },
    legend: { enabled: false },
    mapNavigation: { enabled: false },
    credits: { enabled: false },
    plotOptions: { series: { animation: { duration: 500 } } },
    series: [{
      name: "Countries",
      mapData: mapData,
      color: "#E0E0E0",
      enableMouseTracking: false
    }, {
      type: "mapbubble",
      mapData: mapData,
      name: "Number of installs",
      joinBy: ["iso-a2", "code"],
      minSize: 4,
      maxSize: "12%",
      tooltip: {
        pointFormat: "{point.code}: {point.z}"
      }
    }]
  });

  $("#devices").highcharts({
    credits: { enabled: false },
    chart: {
      plotBackgroundColor: null,
      plotBorderWidth: null,
      plotShadow: false,
      type: "pie"
    },
    title: { text: "" },
    plotOptions: {
      pie: {
        allowPointSelect: true,
        cursor: "pointer",
        size: "45%",
        dataLabels: {
          distance: 10,
          enabled: true,
          style: {
            color: "rgb(119,119,119)",
            textShadow: false
          }
        }
      }
    },
    series: [{
      name: "Installs",
      colorByPoint: true,
      data: [
        { name: "iPhone", y: 991 },
        { name: "iPad", y: 457 },
        { name: "GT-I9300", y: 143 },
        { name: "SM-G530H", y: 102 },
        { name: "GT-I9500", y: 83 },
      ]
    }]
  });

  connect("com.lock.app");
});
