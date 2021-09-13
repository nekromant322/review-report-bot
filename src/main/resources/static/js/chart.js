var ctxPerDay = document.getElementById('myChart').getContext('2d');
var ctxPerWeek = document.getElementById('myChart2').getContext('2d');
// console.log(getStat())
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
// var myChart = new Chart(ctx, {
//     type: 'line',
//     data: {
//         labels: ['Red', 'Blue', 'Yellow', 'Green', 'Purple', 'Orange'],
//         datasets: [{
//             label: '# of Votes',
//             data: [12, 19, 3, 5, 2, 3],
//             backgroundColor: [
//
//                 'rgba(54, 162, 235, 0)',
//             ],
//             borderColor: [
//
//                 'rgba(54, 162, 235, 1)',
//             ],
//             borderWidth: 1
//         }]
//     },
//     options: {
//         scales: {
//             y: {
//                 beginAtZero: true
//             }
//         }
//     }
// });


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