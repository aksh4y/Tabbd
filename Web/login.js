// Token auth

window.onload = function () {

    // If token is registered, redirect to popup
    chrome.cookies.get({"url": "https://akshaysadarangani.com/Tabbd", "name": "Token"}, function (cookie) {
        if(cookie)
            location.href="popup.html";
    });


    document.getElementById('authenticate').onclick = function () {
        var creator = document.getElementById('token').value;
        chrome.cookies.set({
            "name": "Token",
            "url": "https://akshaysadarangani.com/Tabbd",
            "expirationDate": (new Date().getTime() / 1000) + 31557600,
            "value": creator
        }, function () {
            location.href='popup.html';
        });
    }
};