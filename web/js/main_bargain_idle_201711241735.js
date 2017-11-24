function timer() {
    send_request("GET", basePath
        + "studentpage/main_bargain_idle_ajax.jsp?action=timer&time="
        + Math.random(), null, "text", pushData);
    setTimeout("timer()", 1000);
// setTimeout(function() {
// send_request("GET", basePath
// + "studentpage/main_bargain_ajax.jsp?action=timer&time="
// + Math.random(), null, "text", pushData);
// }, 1000)
}

function pushData() {
    if (http_request.readyState == 4) { // �0�2�0�8��0�3�0�2�0�2�0�2�0�4�0�1���0�3�0�4�0�0�0�9�0�3�0�0�0�1
        // console.log("aaa");
        if (http_request.status == 200) { // �0�1�0�7�0�3�0�3�0�1�0�4�0�2���0�5�0�4�0�3�0�5�0�3�0�8�0�6�0�2�0�0�0�1���0�7�0�0�0�2�0�7�0�0�0�7�0�4�0�2�0�2�0�4�0�0�0�2��0�1�0�2��0�4�0�4�0�6�0�6�0�1�0�7�0�3�0�3�0�1�0�4
            try {
                var returnObj = eval("(" + http_request.responseText + ")");
                var action = returnObj.action;
                if (action == "timer") {
                    var status = returnObj.status;
                    if (status == "finished") {
                        window.location.href = basePath + "servlet/transform"
                    } else if (status == "unfinished") {
                        var remainingTime = returnObj.remainingTime;
                        var obj = document.getElementById("timer");
                        obj.innerHTML = Trim(remainingTime);
                    } else if (status == "updating") {
                        var obj = document.getElementById("timer");
                        obj.innerHTML = "";
                    } else if (status == '实验已结束') {
                        alert("实验已结束")
                    } else if (status == "空闲中") {
                        // window.location.href = basePath
                        // + "servlet/bargainTransform?status=" + status;
                    } else if (status == "谈判中") {
                        window.location.href = basePath
                            + "servlet/bargainTransform?status=" + status;
                    } else if (status == "谈判完毕") {
                        window.location.href = basePath
                            + "servlet/bargainTransform?status=" + status;
                    }
                } else if (action == '重新登录') {
                    window.location.href = basePath + "studentpage/login.jsp";
                } else {
                    console.log("1");
                    window.location.href = basePath + "studentpage/login.jsp";
                }
            } catch (e) {
                console.log(http_request.responseText);
                window.location.href = basePath + "studentpage/login.jsp";
            }
        } else { // ajax发送失败,重新登录
            console.log("2");
            window.location.href = basePath + "studentpage/login.jsp";
        }
    }
}