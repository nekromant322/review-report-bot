var ctxPerDay = document.getElementById('myChart').getContext('2d');
var ctxPerWeek = document.getElementById('myChart2').getContext('2d');
var ctxSteps = document.getElementById('myChart3').getContext('2d');

function done() {
    var url = myChart.toBase64Image();

    console.log(url);
    updatePerDayPhoto(url);
}


let statPerDay = getStatPerDay()
var myChart = new Chart(ctxPerDay, {
    type: 'line',
    data: {
        labels: statPerDay.labels,
        datasets: statPerDay.userStats
    },
    options: {
        scales: {
            y: {
                beginAtZero: true
            }
        },
        animation: {
            onComplete: done
        }
    }
});

let statPerWeek = getStatPerWeek()
var myChartPerWeek = new Chart(ctxPerWeek, {
    type: 'line',
    data: {
        labels: statPerWeek.labels,
        datasets: statPerWeek.userStats
    },
    options: {
        scales: {
            y: {
                beginAtZero: true
            }
        }
    }
});

let statForSteps = getStatForSteps()
var myStepsChart = new Chart(ctxSteps, {
    type: 'bar',
    data: {
        labels: statForSteps.labels,
        datasets: statForSteps.userStats
    },
    options: {
        scales: {
            yAxes: [{
                ticks: {
                    beginAtZero: true
                }
            }]
        }
    }
});


function getStatPerDay() {
    let data;
    $.ajax({
        method: 'GET',
        url: "/statPerDay",
        async: false,
        success: function (response) {
            console.log(response)
            data = response;
        },
        error: function (error) {
            console.log(error);
        }
    });
    return data;
}

function getStatPerWeek() {
    let data;
    $.ajax({
        method: 'GET',
        url: "/statPerWeek",
        async: false,
        success: function (response) {
            console.log(response)
            data = response;
        },
        error: function (error) {
            console.log(error);
        }
    });
    return data;
}

function getStatForSteps() {
    let data;
    $.ajax({
        method: 'GET',
        url: "/statStep",
        async: false,
        success: function (response) {
            console.log(response)
            data = response;
        },
        error: function (error) {
            console.log(error);
        }
    });
    return data;
}

function updatePerDayPhoto(encodedPhotoBase64) {
    $.ajax({
        url: "/updatePerDayPhoto",
        dataType: 'json',
        type: 'POST',
        contentType: "application/json",
        data: JSON.stringify({encodedPhoto: encodedPhotoBase64}),
        async: false,
        success: function (response) {
            console.log(response)
        },
        error: function (error) {
            console.log(error);
        }
    });

}