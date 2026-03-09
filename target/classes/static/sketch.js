let buildData = null;
let cachedImages = {};
let gameVersion = "15.4.1";
let showExportMenu = false;
let exportOptions = ["Discord", "League"];
let selectedExportOption = 0;

function setup() {
    createCanvas(500, 600);
    loadVersion();
}

function draw() {
    background(240);
    
    if (buildData === null) {
        // Welcome screen
        fill(0);
        textSize(32);
        textAlign(CENTER, CENTER);
        text("Bravery Alive", width / 2, height / 2 - 100);
        
        textSize(16);
        text("Click 'Roll' to generate a random build", width / 2, height / 2);
    } else {
        // Draw build display
        drawBuild();
    }
    
    drawButtons();
    
    if (showExportMenu) {
        drawExportMenu();
    }
}

function drawBuild() {
    fill(0);
    textSize(14);
    textAlign(LEFT, TOP);
    
    // Champion section
    stroke(180, 180, 180);
    strokeWeight(2);
    fill(255);
    rect(20, 20, 460, 100);
    
    fill(0);
    textSize(12);
    text("Champion", 30, 30);
    textSize(16);
    textAlign(CENTER);
    text(buildData.champion, width / 2, 50);
    
    // Display champion image
    if (buildData.championImageUrl && cachedImages[buildData.championImageUrl]) {
        image(cachedImages[buildData.championImageUrl], 30, 65, 40, 40);
    }
    
    // Mastery & Skills section
    textAlign(LEFT);
    textSize(12);
    text("Mastery: " + buildData.mastery, 100, 75);
    text("Skill To Max: " + buildData.skillToMax, 100, 95);
    text("Summoners: " + buildData.summonerSpell1 + " / " + buildData.summonerSpell2, 100, 115);
    
    // Items section
    textSize(14);
    text("Items", 30, 150);
    
    stroke(180, 180, 180);
    strokeWeight(2);
    fill(255);
    rect(20, 160, 460, 300);
    
    textAlign(LEFT);
    textSize(12);
    let yPos = 170;
    
    // Boots
    if (buildData.bootsImageUrl && cachedImages[buildData.bootsImageUrl]) {
        image(cachedImages[buildData.bootsImageUrl], 30, yPos, 30, 30);
    }
    text(buildData.boots, 70, yPos + 8);
    yPos += 40;
    
    // Legendary items
    for (let i = 0; i < buildData.items.length; i++) {
        if (buildData.itemImageUrls[i] && cachedImages[buildData.itemImageUrls[i]]) {
            image(cachedImages[buildData.itemImageUrls[i]], 30, yPos, 30, 30);
        }
        text(buildData.items[i], 70, yPos + 8);
        yPos += 40;
    }
}

function drawButtons() {
    // Roll button
    drawButton(125, 550, "Roll");
    
    // Copy button
    drawButton(375, 550, "Copy");
    
    // Export button (dropdown)
    drawButton(250, 550, "Export");
}

function drawButton(x, y, label) {
    fill(255);
    stroke(180, 180, 180);
    strokeWeight(2);
    rect(x - 25, y - 25, 50, 50);
    
    fill(0);
    textAlign(CENTER, CENTER);
    textSize(12);
    text(label, x, y);
}

function drawExportMenu() {
    // Semi-transparent overlay
    fill(0, 0, 0, 100);
    rect(0, 0, width, height);
    
    // Menu box
    fill(255);
    stroke(0);
    strokeWeight(2);
    let menuWidth = 150;
    let menuHeight = 80;
    let menuX = (width - menuWidth) / 2;
    let menuY = (height - menuHeight) / 2;
    
    rect(menuX, menuY, menuWidth, menuHeight);
    
    // Options
    fill(0);
    textAlign(CENTER, CENTER);
    textSize(12);
    
    for (let i = 0; i < exportOptions.length; i++) {
        let optionY = menuY + 20 + i * 25;
        
        // Highlight selected option
        if (i === selectedExportOption) {
            fill(200, 200, 255);
            rect(menuX + 5, optionY - 10, menuWidth - 10, 20);
        }
        
        fill(0);
        text(exportOptions[i], width / 2, optionY);
    }
}

function mousePressed() {
    if (showExportMenu) {
        let menuWidth = 150;
        let menuHeight = 80;
        let menuX = (width - menuWidth) / 2;
        let menuY = (height - menuHeight) / 2;
        
        // Check if click is on export menu
        if (mouseX > menuX && mouseX < menuX + menuWidth &&
            mouseY > menuY && mouseY < menuY + menuHeight) {
            
            // Determine which option was clicked
            for (let i = 0; i < exportOptions.length; i++) {
                let optionY = menuY + 20 + i * 25;
                if (mouseY > optionY - 10 && mouseY < optionY + 10) {
                    exportBuild(exportOptions[i]);
                    showExportMenu = false;
                    return;
                }
            }
        }
        
        showExportMenu = false;
        return;
    }
    
    // Roll button
    if (mouseX > 100 && mouseX < 150 && mouseY > 525 && mouseY < 575) {
        rollNewBuild();
        return;
    }
    
    // Copy button
    if (mouseX > 350 && mouseX < 400 && mouseY > 525 && mouseY < 575) {
        if (buildData) {
            copyToClipboard(buildData);
        }
        return;
    }
    
    // Export button
    if (mouseX > 225 && mouseX < 275 && mouseY > 525 && mouseY < 575) {
        if (buildData) {
            showExportMenu = !showExportMenu;
        }
        return;
    }
}

function rollNewBuild() {
    fetch('/api/roll')
        .then(response => response.json())
        .then(data => {
            buildData = data;
            preloadImages();
        })
        .catch(error => {
            console.error('Error rolling build:', error);
            alert('Error generating build. Please try again.');
        });
}

function preloadImages() {
    if (buildData.championImageUrl && !cachedImages[buildData.championImageUrl]) {
        cachedImages[buildData.championImageUrl] = createImage(40, 40);
        loadImage(buildData.championImageUrl, img => {
            cachedImages[buildData.championImageUrl] = img;
        });
    }
    
    if (buildData.bootsImageUrl && !cachedImages[buildData.bootsImageUrl]) {
        cachedImages[buildData.bootsImageUrl] = createImage(30, 30);
        loadImage(buildData.bootsImageUrl, img => {
            cachedImages[buildData.bootsImageUrl] = img;
        });
    }
    
    for (let i = 0; i < buildData.itemImageUrls.length; i++) {
        let url = buildData.itemImageUrls[i];
        if (url && !cachedImages[url]) {
            cachedImages[url] = createImage(30, 30);
            loadImage(url, img => {
                cachedImages[url] = img;
            });
        }
    }
}

function copyToClipboard(build) {
    let text = `Champion: ${build.champion}\nMastery: ${build.mastery}\nSummoners: ${build.summonerSpell1} / ${build.summonerSpell2}\nSkill to Max: ${build.skillToMax}\nItems:\n- ${build.boots}\n- ${build.items.join('\n- ')}`;
    
    navigator.clipboard.writeText(text).then(() => {
        alert('Build copied to clipboard!');
    }).catch(error => {
        console.error('Error copying to clipboard:', error);
        alert('Error copying to clipboard.');
    });
}

function exportBuild(format) {
    fetch(`/api/export/${format.toLowerCase()}`)
        .then(response => response.json())
        .then(data => {
            navigator.clipboard.writeText(data.content).then(() => {
                alert(`${format} format copied to clipboard!`);
            }).catch(error => {
                console.error('Clipboard error:', error);
                alert('Error copying to clipboard.');
            });
        })
        .catch(error => {
            console.error('Export error:', error);
            alert(`Error exporting to ${format} format.`);
        });
}

function loadVersion() {
    fetch('/api/version')
        .then(response => response.text())
        .then(version => {
            gameVersion = version;
        })
        .catch(error => {
            console.error('Error loading version:', error);
        });
}
