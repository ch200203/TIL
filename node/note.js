// const app = require('express')();
// const http = require('http').Server(app);
// const io = require('socket.io')(http);

const express = require('express');
const app = express();
const http = require('http');
const server = http.createServer(app);
const { Server } = require('socket.io');
const io = new Server(server);

app.get('/', (req, res) => {
    res.send("Hello world");
});


// socket이 붙어 있는 http 서버에 connection 발생
// 클라이언트 connect 성공 시 호출
io.on('connection', (socket) => {
    console.log('client connected');

    // 클라이언트에서 'message'라는 이름의 이벤트 발생
    // msg 받아옴
    socket.on('message', (msg)=> {
        // message 이벤트 처리   
        console.log('server received data');
        console.log(msg);

        // postman 테스트용
        io.emit('return_event', {
            'msg' : "message title"
        });
        
        // msg에 받을 사람 정보(user_id, 제목 넣어서 구현)
        // msg에 toMsg담아서 리턴
        // 인터페이스 구성 후 테스트 해볼것.
        // let arr_users = new Array();
        // arr_users.forEach(i => io.emit('toMsg', msg));
        // socket.to(user_id).emit('전달할 메시지');
        
    });

    socket.on('disconnect', () => {
        // 클라이언트 disconnect 처리
        console.log('server disconnected');
    });

    socket.on('error', () => {
        // WEB 소켓 에러 처리
        console.log("WEB SOCKET ERROR!!!");
    });


    socket.on('chat', function(msg){
        io.emit('chat', 'message 다시 돌려줌');
    });    
});

// 소켓통신 포트 지정
const PORT = 3000;
// http.listen(PORT, () => { console.log('listening port :: ' + PORT); });
server.listen(PORT, () => {
    console.log('listening port :: ' + PORT);
});


