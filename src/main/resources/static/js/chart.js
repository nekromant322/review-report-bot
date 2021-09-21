var ctxPerDay = document.getElementById('perDayChart').getContext('2d');
var ctxSteps = document.getElementById('stepsChart').getContext('2d');
var ctxSalary = document.getElementById('salaryChart').getContext('2d');
let urlUpdated = false;

function done() {
    if (myChart.data.datasets.length > 1 && !urlUpdated) {
        var url = myChart.toBase64Image();
        updatePerDayPhoto(url);
        urlUpdated = true
    }
}


let statPerDay = getStatPerDay()
let statPerWeek = getStatPerWeek()
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
            text: 'Часов на учебу'
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

var statSalary = getStatForSalary();
var salaryChart = new Chart(ctxSalary, {
    type: 'line',
    data: {
        labels: statSalary.labels,
        datasets: statSalary.userStats
    },
    options: {
        responsive: true,
        interaction: {
            intersect: false,
            axis: 'x'
        },
        plugins: {
            title: {
                display: true,
                text: "Зарплаты"
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

function getStatForSalary() {
    let data;
    $.ajax({
        method: 'GET',
        url: "/statSalary",
        async: false,
        success: function (response) {
            console.log(response)
            data = response;
        },
        error: function (error) {
            console.log(error);
        }
    });


    for (let i = 0; i < data.userStats.length; i++) {
        for (let j = 0; j < data.userStats[i].data.length; j++) {
            if (data.userStats[i].data[j] === 0) {
                data.userStats[i].data[j] = "N/A";
            }
        }
    }
    console.log("after process");
    console.log(data);
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
            // console.log(response)
        },
        error: function (error) {
            console.log(error);
        }
    });

}

function drawPerDay() {
    myChart.data.datasets = statPerDay.userStats;
    myChart.data.labels = statPerDay.labels
    myChart.update();
}

function drawPerWeek() {
    myChart.data.datasets = statPerWeek.userStats;
    myChart.data.labels = statPerWeek.labels
    myChart.update();
}