const pdfInput = document.getElementById('pdf'),
    clearInput = document.getElementById('clear-input'),
    pdfLabel = document.getElementById('pdf-label'),
    burger = document.getElementById('burger'),
    headerLinks = document.getElementById('header-links'),
    linksClose = document.getElementById('links-close'),
    resumeButton = document.getElementById('resume-button'),
    resume = document.getElementById('resume'),
    mentoringButton = document.getElementById('mentoring-button'),
    mentoring = document.getElementById('mentoring'),
    callButton = document.getElementById('call-button'),
    call = document.getElementById('call'),
    popup = document.getElementById('popup'),
    popupTitle = popup.querySelector('.popup__content__header__title'),
    showUpgradeButton = document.getElementById('show-upgrade-button'),
    showMentoringButton = document.getElementById('show-mentoring-button'),
    showCallButton = document.getElementById('show-call-button'),
    popupClose = document.getElementById('popup-close'),
    form = document.getElementById('form'),
    pdfField = form.querySelector('.field_pdf');

clearInput.addEventListener('click', () => {
    try {
        pdfInput.value = null;
    } catch (ex) {}
    if (pdfInput.value) {
        pdfInput.parentNode.replaceChild(pdfInput.cloneNode(true), pdfInput);
    }
    pdfLabel.innerText = 'выберите файл';
});

pdfInput.addEventListener('change', () => {
    document.getElementById('pdf-label').innerText = pdfInput.files[0].name;
});

burger.addEventListener('click', () => {
    headerLinks.classList.toggle('header__links_active');
});

linksClose.addEventListener('click', () => {
    headerLinks.classList.remove('header__links_active');
});

resumeButton.addEventListener('click', () => {
    resume.scrollIntoView({behavior: 'smooth'});
});

mentoringButton.addEventListener('click', () => {
    mentoring.scrollIntoView({behavior: 'smooth'});
});

callButton.addEventListener('click', () => {
    call.scrollIntoView({behavior: 'smooth'});
});

showUpgradeButton.addEventListener('click', () => {
    popup.classList.remove('popup_hidden');
    popupTitle.innerText = 'Апгрейд резюме';
    pdfField.style.display = 'flex';
    pdfInput.required = true;
});

showCallButton.addEventListener('click', () => {
    popup.classList.remove('popup_hidden');
    popupTitle.innerText = 'Созвон';
    pdfField.style.display = 'none';
    pdfInput.required = false;
});

showMentoringButton.addEventListener('click', () => {
    popup.classList.remove('popup_hidden');
    popupTitle.innerText = 'Менторинг';
    pdfField.style.display = 'none';
    pdfInput.required = false;
});

popupClose.addEventListener('click', () => {
    popup.classList.add('popup_hidden');
    form.reset();
});

form.addEventListener('submit', (event) => {
    popup.classList.add('popup_hidden');
    form.reset();
});
