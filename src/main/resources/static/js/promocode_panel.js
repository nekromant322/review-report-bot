getPromocodeList();

async function getPromocodeList() {
    await fetch("./promocode")
        .then(response => response.json())
        .then(promocodeList => {
            let table = document.getElementById("promocode-table-body");
            table.innerHTML = ``;
            localStorage.setItem("promocodeList", JSON.stringify(promocodeList));
            for (let i = 0; i < promocodeList.length; i++) {
                table.innerHTML += `
            <td>${promocodeList[i].promocodeText}</td>
            <td>${promocodeList[i].discountPercent}</td>
            <td>${promocodeList[i].counterUsed}</td>
            <td>${promocodeList[i].maxUsesNumber - promocodeList[i].counterUsed}</td>
            <td>${promocodeList[i].created}</td>
            <td><a href="" onclick="getLifePayTransactionsSet(${i})">Посмотреть</a></td>
            <td><input type="checkbox" id="${promocodeList[i].id}_isActive" onclick="updateSingleIsActive(${promocodeList[i].id})"></td>
            <td><button onclick="deletePromocode(${promocodeList[i].id})">Удалить</button></td>
            `;
            }
            for (let i = 0; i < promocodeList.length; i++) {
                if (promocodeList[i].active) {
                    document.getElementById(promocodeList[i].id + '_isActive').checked = true;
                }
            }
        })
}

async function savePromocode() {
    // let promocodeList = JSON.parse(localStorage.getItem('promocodeList'));
    let promocodeText = document.getElementById("promocodeText").value;
    if (promocodeText.length === 0) {
        alert('должен быть текст');
        return;
    }

    let response = await fetch("promocode?text=" + promocodeText)
    if (await response.status != 404) {
        alert("Текст промокода должен быть уникальным!");
        return;
    }

    let discountPercent = document.getElementById("discountPercent").value;
    if (discountPercent >= 100) {
        alert('Кто кому платить собирается??');
        return;
    }
    if (discountPercent < 0) {
        alert('Чет странное');
        return;
    }

    let maxUsesNumber = document.getElementById("maxUsesNumber").value;
    if (maxUsesNumber <= 0) {
        alert('И зачем такой промокод нужен?');
        return;
    }

    let isActive = document.getElementById("isActive").checked;
    let newPromoCode = {
        promocodeText: promocodeText,
        discountPercent: discountPercent,
        maxUsesNumber: maxUsesNumber,
        isActive: isActive
    };
    await fetch("./promocode", {
        method: "POST",
        body: JSON.stringify(newPromoCode),
        headers: {
            "Content-Type": "application/json; charset=UTF-8"
        }
    });
    getPromocodeList();
}

async function deletePromocode(promocode_id) {
    await fetch("./promocode", {
        method: "DELETE",
        body: JSON.stringify({
            promocode_id: promocode_id
        }),
        headers: {
            "Content-Type": "application/json; charset=UTF-8"
        }
    });
    getPromocodeList();
}

async function updateSingleIsActive(promocode_id) {
    let isActive = document.getElementById(promocode_id + "_isActive").checked;
    await fetch("./promocode/isactive", {
        method: "PATCH",
        body: JSON.stringify({
            promocode_id: promocode_id,
            isActive: isActive
        }),
        headers: {
            "Content-Type": "application/json; charset=UTF-8"
        }
    });
}

async function getLifePayTransactionsSet(i) {
    let lifePayTransactions = JSON.parse(localStorage.getItem("promocodeList"))[i].paymentDetailsSet;
    const jsString = JSON.stringify(lifePayTransactions);
    const blob = new Blob([jsString], {type: "application/json"});
    const url = URL.createObjectURL(blob);
    window.open(url, '_blank');
    URL.revokeObjectURL(url);
}