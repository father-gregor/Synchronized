
$(function() {
	var stompClient = null;
	function send() {
		stompClient.send("/app/hello", {}, JSON.stringify({"result": "Websocket connection established"}))
	}
	function connect() {
		var socket = new SockJS("/videoroom");
		stompClient = Stomp.over(socket);
		console.log("First");
		stompClient.connect({}, function(frame) {
			console.log("Connected: " + frame);
			//send();
			stompClient.subscribe("/topic/currtime", function(res) {
				console.log(JSON.parse(res.body).result);
			})
		});
	}
	function disconnect() {
		stompClient.disconnect();
	}
	connect();
	var player = videojs("room-player", {
		controls: true,
		autoplay: true,
		techOrder: ["html5", "youtube", "flash"]
	});
	player.ready(function() {
		//player.src({type: "video/webm", src: "link to file"});
	});
});