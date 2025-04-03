document.addEventListener('DOMContentLoaded', () => {
    // Details Toggle
    const detailsToggles = document.querySelectorAll('.details-toggle');
    // Reset button
    const resetButton = document.getElementById('reset-progress');
    if (!resetButton) {
            console.error("Reset button not found in DOM.");
            return;
        }

    // Animate circular progress bars
    const progressBars = document.querySelectorAll('.circular-progress');

    progressBars.forEach(progressBar => {
        const targetProgress = parseFloat(progressBar.dataset.progress); // Get target progress from data attribute
        let currentProgress = 0; // Start from 0

        const interval = setInterval(() => {
            currentProgress += 1; // Increment progress
            if (currentProgress >= targetProgress) {
                currentProgress = targetProgress; // Stop at target progress
                clearInterval(interval); // Clear interval
            }

            // Update circular progress bar background and text
            progressBar.style.background = `conic-gradient(#3498db ${currentProgress * 3.6}deg, #ededed 0deg)`;
            const progressValue = progressBar.querySelector('.progress-value');
            if (progressValue) {
                progressValue.textContent = `${currentProgress.toFixed(1)}%`;
            }
        }, 20); // Adjust speed of animation (20ms per increment)
    });


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

    // Initial data fetch with cache control
    fetch(`/monitor/api/lines/progress/all?${Date.now()}`, {
        headers: {
            'Cache-Control': 'no-cache, no-store, must-revalidate',
            'Pragma': 'no-cache',
            'Expires': '0'
        }
    })
    .then(response => {
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        return response.json();
    })
    .then(data => {
        console.log("Initial progress data:", data);
        data.forEach(updateProgressBar);
        data.forEach(line => {
            if (line.planStatus.toLowerCase() === 'in_progress') { // Use planStatus
                startProgressPolling(line.productionLineCode);
            }
        });
    })
    .catch(error => console.error('Error fetching initial progress:', error));

    // Handle tab visibility changes
    document.addEventListener('visibilitychange', () => {
        if (!document.hidden) {
            fetch(`/monitor/api/lines/progress/all?${Date.now()}`)
                .then(response => {
                    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                    return response.json();
                })
                .then(data => {
                    console.log("Initial progress data:", data);
                    data.forEach(updateProgressBar);
                    data.forEach(line => {
                        if (line.planStatus.toLowerCase() === 'in_progress') { // Use planStatus
                            startProgressPolling(line.productionLineCode);
                        }
                    });
                })
                .catch(error => console.error('Error fetching initial progress:', error));
        }
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
            if (data.planStatus) {
                statusElement.textContent = data.planStatus;
                statusElement.className = `status ${data.planStatus.toLowerCase().replace('_', '-')}`;
            } else {
                console.error("planStatus is undefined or invalid:", data.planStatus);
                statusElement.textContent = "Unknown";
                statusElement.className = "status unknown";
            }

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

    resetButton.addEventListener('click', () => {
        console.log(resetButton);
        if (confirm("Are you sure you want to reset all progress? This action cannot be undone.")) {
            fetch('/monitor/reset', { method: 'POST' })
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`HTTP error! status: ${response.status}`);
                    }
                    return response.text();
                })
                .then(message => {
                    alert(message); // Notify user of successful reset

                    // Reset progress in the UI
                    const lineCards = document.querySelectorAll('.production-line-card');
                    lineCards.forEach(lineCard => {
                        const progressElement = lineCard.querySelector('.circular-progress');
                        const progressValueElement = lineCard.querySelector('.progress-value');
                        const statusElement = lineCard.querySelector('.status');

                        if (progressElement) {
                            updateCircularProgress(progressElement, 0); // Reset circular progress
                        }
                        if (progressValueElement) {
                            progressValueElement.textContent = "0.0%"; // Reset text progress
                        }
                        if (statusElement) {
                            statusElement.textContent = "STANDBY"; // Reset status to default
                            statusElement.className = "status standby"; // Update CSS class
                        }
                    });

                    console.log("All progress has been reset.");
                })
                .catch(error => console.error('Error resetting progress:', error));
        }
    });
});
