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
                updateProgress();  // Update progress immediately after starting simulation
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

    // Polling function to update progress bars and statuses dynamically
    function startProgressPolling() {
        setInterval(function () {
            fetch('/monitor/api/lines/progress')
                .then(response => response.json())
                .then(data => {
                    data.forEach(line => {
                        const progressElement = document.querySelector(`.production-line-card[data-line-code="${line.productionLineCode}"] .circular-progress`);
                        const progressValueElement = document.querySelector(`.production-line-card[data-line-code="${line.productionLineCode}"] .progress-value`);
                        const statusElement = document.querySelector(`.production-line-card[data-line-code="${line.productionLineCode}"] .status`);

                        if (progressElement && progressValueElement && statusElement) {
                            const cappedProgress = Math.min(line.progress, 100).toFixed(1); // Cap at 100%
                            progressElement.dataset.progress = cappedProgress;
                            progressValueElement.textContent = `${cappedProgress}%`;
                            progressElement.style.background = `conic-gradient(#3498db ${cappedProgress * 3.6}deg, #ededed 0deg)`;

                            statusElement.textContent = line.status;
                            statusElement.className = `status ${line.status.toLowerCase()}`;
                        }
                    });
                })
                .catch(error => console.error('Error fetching line progress:', error));
        }, 3000); // Poll every 3 seconds to match backend updates
    }

    startProgressPolling();
});
