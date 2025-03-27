document.addEventListener('DOMContentLoaded', () => {
    // Circular Progress Animation
//    const circularProgressElements = document.querySelectorAll('.circular-progress');
//
//    circularProgressElements.forEach(element => {
//        const progressValue = element.querySelector('.progress-value');
//        const progress = parseInt(element.dataset.progress);
//
//        let startValue = 0;
//        let endValue = progress;
//
//        let animationId = setInterval(() => {
//            startValue++;
//            progressValue.textContent = `${startValue}%`;
//            element.style.background = `conic-gradient(#3498db ${startValue * 3.6}deg, #ededed 0deg)`;
//
//            if (startValue === endValue) {
//                clearInterval(animationId);
//            }
//        }, 20);
//    });

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

    // Simulation trigger
    const simulateButtons = document.querySelectorAll('.simulate-button');
    simulateButtons.forEach(button => {
        button.addEventListener('click', () => {
            const lineCode = button.dataset.lineCode;
            $.get(`/monitor/simulate/${lineCode}`, () => {
                alert('Simulation started for line: ' + lineCode);
                startProgressPolling(lineCode);  // Start polling for this specific line
            }).fail(function(jqXHR, textStatus, errorThrown) {
                console.error("Error starting simulation:", textStatus, errorThrown);
                alert('Failed to start simulation. Please check the console for more details.');
            });
        });
    });


    // Function to update circular progress
    function updateCircularProgress(element, progress) {
        const progressValue = element.querySelector('.progress-value');
        progressValue.textContent = `${progress.toFixed(1)}%`;
        element.style.background = `conic-gradient(#3498db ${progress * 3.6}deg, #ededed 0deg)`;
    }

    // Polling function to update progress for a specific line
    function startProgressPolling(lineCode) {
        const poller = setInterval(() => {
            fetch(`/monitor/api/lines/progress/${lineCode}`)
                .then(response => response.json())
                .then(data => {
                    updateProgressBar(data);
                    if (data.progress >= 100) clearInterval(poller);
                });
        }, 1000);  // Poll every second
    }
});
