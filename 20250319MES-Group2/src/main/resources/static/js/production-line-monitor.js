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

    // Periodic progress update
    function updateProgress() {
        $.get('/monitor/api/lines/progress', (data) => {
            data.forEach(line => {
                const lineCard = document.querySelector(`.production-line-card[data-line-code="${line.productionLineCode}"]`);
                if (lineCard) {
                    const progressElement = lineCard.querySelector('.circular-progress');
                    const statusElement = lineCard.querySelector('.status');

                    // Use the progress value from the server
                    updateCircularProgress(progressElement, line.progress);

                    statusElement.textContent = line.status;
                    statusElement.className = `status ${line.status.toLowerCase()}`;

                    // Disable simulate button if the line is DEFECTED
                    const simulateButton = lineCard.querySelector('.simulate-button');
                    simulateButton.disabled = (line.status === 'DEFECTED');
                }
            });
        });
    }

    // Initial update
    updateProgress();

    // Update progress every 2 seconds
    setInterval(updateProgress, 2000);
});
