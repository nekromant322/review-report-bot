$(document).ready(function () {
    updateTable();
    setInterval(function () {
        updateTable();
    }, 60 * 1000);

});

function updateTable() {
    let reviewList = getReview();
    $("#tableBody").empty();

    for (let review of reviewList) {
        $("#tableBody").append("" +
            "<tr" + (review.tooLate ? " style=\"bgcolor=\"#FF0000 !important; width=\"100%\"\"" : "") + ">\n" +
            "      <td><a href=\"" + review.studentTgLink + "\"" + ">" + review.studentUserName + "</a></td>\n" +
            "      <td" + (review.tooLate ? " style=\"color:red\"" : review.today ? "" : " style=\"color:green\"") + ">" + review.mentorUserName + "</td>\n" +
            "      <td" + (review.tooLate ? " style=\"color:red\"" : review.today ? "" : " style=\"color:green\"") + ">" + review.title + "</td>\n" +
            "      <td" + (review.tooLate ? " style=\"color:red\"" : review.today ? "" : " style=\"color:green\"") + ">" + review.bookedDateTime + "</td>\n" +
            "      <td" + (review.tooLate ? " style=\"color:red\"" : review.today ? "" : " style=\"color:green\"") + ">" + "<a" +
            " href=\"" + review.roomLink + "\">room</a></td>\n" +
            "    </tr>")
    }

}

function getReview() {
    let reviewList;
    $.ajax({
        url: "/incoming-review",
        dataType: 'json',
        type: 'GET',
        contentType: "application/json",
        async: false,
        success: function (response) {
            reviewList = response;
        }
        // console.log(response)
    })
    return reviewList;
}