// Global variable to store cached user ID
var userID, myRef;

window.onload = function () {
    // Initialize Firebase
    var config = {
        apiKey: config.apiKey,
        authDomain: config.authDomain,
        databaseURL: config.databaseURL,
        projectId: config.projectId,
        storageBucket: config.storageBucket,
        messagingSenderId: config.messagingSenderId
    };
    firebase.initializeApp(config);

    chrome.cookies.get({"url": "https://akshaysadarangani.com/Tabbd", "name": "Token"}, function (cookie) {
        if(cookie) {
            $('#loader').show();
            if (navigator.onLine) {
                setOnline();
                userID = cookie.value;
                myRef = firebase.database().ref('websites').orderByChild('creator').equalTo(userID);
                getWebsites();
            } else {
                setOffline();
                getCachedWebsites();
            }
        }
        else
            location.href = 'login.html';
    });

    $('#signOut').click(signOut);
    $('#openTabs').click(openAllTabs);
    $('#add').click(addWebsite);
    $('#close').click(function () {
        $('.alert').hide();
    });
};

window.addEventListener('online', function(e) {
    // Re-sync data with server.
    $('#loader').show();
    getWebsites();
    setOnline();
}, false);

window.addEventListener('offline', function(e) {
    setOffline();
}, false);

function setOnline() {
    $('#status').css('color', 'green');
    $('#connectivity').css('color', 'green');
    $('#connectivity').text('CONNECTED');
    $('#sync').css('color', 'green');
    $('#sync').text("Syncing...");
    $('.glyphicon-plus').removeClass('disabled');
    $('.glyphicon-plus').prop('onclick', null).off('click');
    $('.glyphicon-plus').click(showModal);
}

function setOffline() {
    $('#status').css('color', 'red');
    $('#connectivity').text('DISCONNECTED');
    $('#connectivity').css('color', 'red');
    $('#sync').css('color', 'red');
    $('.glyphicon-plus').prop('onclick', null).off('click');
    $('.glyphicon-plus').addClass('disabled');
    $('.col-xs-1').prop('onclick', null).off('click');
    $('.glyphicon-remove').addClass('disabled');
    $('.col-xs-1').addClass('disabled');
    // Alert offline
    $('.disabled').click(function () {
        $('#offlineAlert').show();
    });
}

function signOut() {
    chrome.cookies.remove({'url': 'https://akshaysadarangani.com/Tabbd', 'name': 'Token'}, function () {
        location.href = 'login.html';
    });
}

function showModal() {
    $('#myModal').modal('show');
    $('#myModal').on("shown.bs.modal", function() {
        $('#url').focus();
    });
}

function addWebsite() {
    var url = $('#url')[0].value;
    // Get a key for a new Post.
    var newWebsite = firebase.database().ref('websites').push().key;
    firebase.database().ref('websites/' + newWebsite).set({
        creator: userID,
        url: url
    });
    $('#myModal').modal('hide');
    $('#url')[0].value='';
}

function getWebsites() {
    $('#openTabs').hide();
    $('#list').empty();
    var websites = [];
    myRef.off('child_added');
    myRef.on('child_added', function (data) {
        buildWebsites(data.val().url, data.key);
        websites.push(data.val().url);
        $('#openTabs').show();

    });

    myRef.once("value")
        .then(function(snapshot) {
            // Cache locally
            chrome.storage.sync.set({'websites': websites}, function() {
                var today = new Date();
                var date = today.getFullYear()+'-'+(today.getMonth()+1)+'-'+today.getDate();
                var time = today.getHours() + ":" + today.getMinutes() + ":" + today.getSeconds();
                var dateTime = date+' '+time;
                chrome.storage.sync.set({'lastSync': dateTime}, function () {
                    $('#sync').text("Last sync: " + dateTime);
                    $('#loader').hide();
                });
            });
        });

    myRef.on('child_changed', function (data) {
        var ele = "#" + data.key;
        $(ele).text(data.val().url);
        var a = document.createElement("a");
        a.textContent = data.val().url;
        a.setAttribute('href', data.val().url);
        a.setAttribute('target', '_blank');
        $(ele).html(a);
    });
    myRef.on('child_removed', function (data) {
        var ele = '#' + data.key;
        $(ele).remove();
    });
}


function getCachedWebsites() {
    var flag = false;
    chrome.storage.sync.get('websites', function (res) {
        $('#list').empty();
        for (var i = 0; i < res.websites.length; i++) {
            buildWebsites(res.websites[i], 'offline');
            flag = true;
        }
        if(flag) {
            $('#openTabs').show();
            $('#openTabs').click(openAllTabs);
        }
    });
    chrome.storage.sync.get('lastSync', function (res) {
        $('#sync').text("Last sync: " + res.lastSync);
        $('#loader').hide();
    });
}

function openAllTabs() {
    var listItems = $("#list li");
    listItems.each(function(idx, li) {
        var url = $("a",$(li)).attr('href');
        chrome.tabs.create({ url: url });
    });
}

function buildWebsites(url, status) {
    var ul = $("#list")[0];
    var li = document.createElement("li");
    li.classList.add('list-group-item');
    var a = document.createElement("a");
    a.textContent = url;
    a.setAttribute('href', url);
    a.setAttribute('target', '_blank');
    var row = document.createElement("div");
    row.classList.add('row');
    var textCol = document.createElement("div");
    textCol.classList.add('col-xs-11');
    textCol.appendChild(a);
    var iconCol = document.createElement("div");
    iconCol.classList.add('col-xs-1');
    var icon = document.createElement("span");
    icon.classList.add('glyphicon');
    icon.classList.add('glyphicon-remove');
    if(status === 'offline') {
        icon.classList.add('disabled');
        icon.onclick = function () {
            $('#offlineAlert').show();
        };
    }
    else {
        li.setAttribute('id', status);
        iconCol.setAttribute('id', status + '_del');
        iconCol.onclick = function () {
            var id = $(this)[0].id;
            id = id.replace('_del', '');
            firebase.database().ref('websites').child(id).remove();
        };
    }
    iconCol.appendChild(icon);
    row.appendChild(textCol);
    row.appendChild(iconCol);
    li.appendChild(row);
    ul.appendChild(li);
}