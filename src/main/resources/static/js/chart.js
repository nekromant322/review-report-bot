var ctxPerDay = document.getElementById('myChart').getContext('2d');
var ctxPerWeek = document.getElementById('myChart2').getContext('2d');
var ctxSteps = document.getElementById('myChart3').getContext('2d');

function done() {
    var url = myChart.toBase64Image();

    updatePerDayPhoto(url);
    if (document.location.search.toString() !== '') {
        let studentUsername = document.location.search.replace("?student=", "");

        myChart.data.datasets = myChart.data.datasets.filter(x => x.label === studentUsername)
        myChart.update();
    }

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
        },
        title: {
            display: true,
            text: 'По дням'
        },
        legend: {
            onClick: function (event, elem) {
                if (myChart.data.datasets.length > 1) {
                    myChart.data.datasets = myChart.data.datasets.filter(x => x.label === elem.text)
                    myChart.update();
                } else {
                    myChart.data.datasets = statPerDay.userStats
                    myChart.update();
                }
            }
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
        },
        title: {
            display: true,
            text: 'По неделям'
        },
        legend: {
            onClick: function (event, elem) {
                if (myChartPerWeek.data.datasets.length > 1) {
                    myChartPerWeek.data.datasets = myChartPerWeek.data.datasets.filter(x => x.label === elem.text)
                    myChartPerWeek.update();
                } else {
                    myChartPerWeek.data.datasets = statPerWeek.userStats
                    myChartPerWeek.update();
                }
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
        },
        title: {
            display: true,
            text: 'Этапы/дни'
        },
        legend: {
            onClick: function (event, elem) {
                if (myStepsChart.data.datasets.length > 1) {
                    myStepsChart.data.datasets = myStepsChart.data.datasets.filter(x => x.label === elem.text)
                    myStepsChart.update();
                } else {
                    myStepsChart.data.datasets = statForSteps.userStats
                    myStepsChart.update();
                }
            }
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