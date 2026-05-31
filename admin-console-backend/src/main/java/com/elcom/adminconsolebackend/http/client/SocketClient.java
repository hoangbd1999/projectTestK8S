package com.elcom.adminconsolebackend.http.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(name = "metacen-socket-service", url = "${socket.service.url}")
public interface SocketClient {

    @PostMapping("/sockets/monitor-warning-message")
    Boolean sendMonitorWarningMessage(@RequestBody Object data);

}
