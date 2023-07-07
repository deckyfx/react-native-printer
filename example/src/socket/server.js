const net = require('net');
const port = 9100;
const host = '0.0.0.0';
const server = net.createServer();
server.listen(port, host, () => {
  console.log('TCP Server is running on port ' + port + '.');
});

let sockets = [];

server.on('connection', (sock) => {
  console.log('CONNECTED: ' + sock.remoteAddress + ':' + sock.remotePort);
  sockets.push(sock);

  sock.on('data', (data) => {
    console.log('DATA ' + sock.remoteAddress + ': ' + data);
    // Write the data back to all the connected, the client will receive it as data from the server
    sockets.forEach((_sock, index, array) => {
      _sock.write(
        _sock.remoteAddress + ':' + _sock.remotePort + ' said ' + data + '\n'
      );
    });
  });
});
