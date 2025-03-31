document.addEventListener('DOMContentLoaded', () => {
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
            console.log("Simulate button clicked for line:", lineCode);
            $.get(`/monitor/simulate/${lineCode}`, () => {
                console.log('Simulation started for line: ', lineCode);
                startProgressPolling(lineCode);  // Start polling for this specific line
            }).fail(function(jqXHR, textStatus, errorThrown) {
                console.error("Error starting simulation:", textStatus, errorThrown);
                alert('Failed to start simulation. Please check the console for more details.');
            });
        });
    });

    // Function to update circular progress
    function updateCircularProgress(element, progress) {
        const numericProgress = Number(progress);
        if (!isNaN(numericProgress)) {
            const progressValue = element.querySelector('.progress-value');
            progressValue.textContent = `${numericProgress.toFixed(1)}%`;
            element.style.background = `conic-gradient(#3498db ${numericProgress * 3.6}deg, #ededed 0deg)`;
        } else {
            console.error('Invalid progress value:', progress);
        }
    }


    // Polling function to update progress for a specific line
    function startProgressPolling(lineCode) {
        console.log("Starting progress polling for line:", lineCode);
        const poller = setInterval(() => {
            fetch(`/monitor/api/lines/progress/${lineCode}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`HTTP error! status: ${response.status}`);
                    }
                    return response.json()})
                .then(data => {
                    updateProgressBar(data);
                    if (data.progress >= 100) clearInterval(poller);
                })
                .catch(error => console.error('Error fetching progress:', error));
        }, 1000);  // Poll every second
    }


    function updateProgressBar(data) {
        console.log("Received progress data:", data);
        const lineCard = document.querySelector(`.production-line-card[data-line-code="${data.productionLineCode}"]`);
        if (lineCard) {
            const progressElement = lineCard.querySelector('.circular-progress');
            const progressValueElement = lineCard.querySelector('.progress-value');
            const statusElement = lineCard.querySelector('.status');

            // Update progress
            console.log("data.progress: ", data.progress);
            const progress = Math.min(Math.max(Number(data.progress), 0), 100).toFixed(1);
            console.log("Calculated progress:", progress);
            updateCircularProgress(progressElement, progress);
            progressValueElement.textContent = `${progress}%`;

            // Update status
            statusElement.textContent = data.status;
            statusElement.className = `status ${data.status.toLowerCase().replace('_', '-')}`;

            // Update capacity and today's production if available
            const capacityElement = lineCard.querySelector('.summary-item:nth-child(1) strong');
            if (capacityElement && data.capacity) {
                capacityElement.textContent = `${data.capacity}/hr`;
            }

            const productionElement = lineCard.querySelector('.summary-item:nth-child(2) strong');
            if (productionElement && data.todayQty) {
                productionElement.textContent = `${data.todayQty} units`;
            }

            // Update current process and task
            const currentProcessElement = lineCard.querySelector('.current-process');
            const currentTaskElement = lineCard.querySelector('.current-task');
            const taskProgressElement = lineCard.querySelector('.task-progress');

            if (currentProcessElement) {
                currentProcessElement.textContent = data.currentProcessType;
            }
            if (currentTaskElement) {
                currentTaskElement.textContent = data.currentTaskType;
            }
            if (taskProgressElement) {
                taskProgressElement.textContent = `${data.currentTaskProgress}%`;
            }
        }
    }
});
