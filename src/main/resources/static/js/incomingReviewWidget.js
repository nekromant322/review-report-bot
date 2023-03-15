$(document).ready(function () {
    let mentor = (new URL(document.location)).searchParams.get("mentor");

    updateTable(mentor);
    setInterval(function () {
        updateTable(mentor);
    }, 60 * 1000);

});

function updateTable(mentor) {
    let reviewList = getReview(mentor);
    $("#tableBody").empty();

    let now = new Date();

    for (let review of reviewList) {
            let dateTime = Date.parse(review.bookedDateTime);
            let oneHourInMilliseconds = 1000 * 60 * 60;
            let isCurrentReview = ((now - dateTime) > 0) & ((now - dateTime) < oneHourInMilliseconds);
            let isPreviousReview = (now - dateTime) > oneHourInMilliseconds;
            $("#tableBody").append("" +
                "<tr" + (isCurrentReview ? " class=\"bg-success p-2 bg-opacity-50\"" : isPreviousReview ? " class=\"text-white text-opacity-75\"" : "") + ">\n" +
                "      <td><a class=\"text-reset\" style=\"text-decoration: none\" href=\"" + review.studentTgLink + "\"" + ">" + review.studentUserName + "</a></td>\n" +
                "      <td>" + review.title + "</td>\n" +
                "      <td>" + review.bookedDateTime.substring(11) + "</td>\n" +
                "    </tr>")
    }

}

function getReview(mentor) {
    let reviewList;
    $.ajax({
        url: "/incoming-review-with-period?mentor=" + mentor,
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