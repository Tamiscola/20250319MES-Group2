document.addEventListener('DOMContentLoaded', () => {
    // Circular Progress Animation
    const circularProgressElements = document.querySelectorAll('.circular-progress');

    circularProgressElements.forEach(element => {
        const progressValue = element.querySelector('.progress-value');
        const progress = parseInt(element.dataset.progress);

        let startValue = 0;
        let endValue = progress;

        let animationId = setInterval(() => {
            startValue++;
            progressValue.textContent = `${startValue}%`;
            element.style.background = `conic-gradient(#3498db ${startValue * 3.6}deg, #ededed 0deg)`;

            if (startValue === endValue) {
                clearInterval(animationId);
            }
        }, 20);
    });

    // Details Toggle
    const detailsToggles = document.querySelectorAll('.details-toggle');

    detailsToggles.forEach(toggle => {
        toggle.addEventListener('click', () => {
            const lineCard = toggle.closest('.production-line-card');
            const details = lineCard.querySelector('.line-details');

            details.classList.toggle('active');
            toggle.textContent = details.classList.contains('active')
                ? 'Hide Details'
                : 'View Details';
        });
    });
});