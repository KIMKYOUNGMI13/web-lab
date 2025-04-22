const eventSource = new EventSource('http://localhost:8080/sse/subscribe');

// customData 라는 이름의 이벤트를 받는 경우
eventSource.addEventListener('customData', function(event) {
    console.log('customData event received:', event.data);
});

// test 라는 이름의 이벤트를 받는 경우
eventSource.addEventListener('test', function(event) {
    console.log('test event received:', event.data);
    document.getElementById('message').innerText = event.data;
});

// onmessage는 이름 없는 기본 이벤트
eventSource.onmessage = function(event) {
    console.log('Default message event received:', event.data);
    // 필요에 따라 기본 처리
};

// SSE 연결이 성공적으로 열렸을 때
eventSource.onopen = function(event) {
    console.log('SSE connection opened.');
};

// SSE 연결 중 에러가 발생했을 때
eventSource.onerror = function(event) {
    console.error('SSE connection error:', event);
};