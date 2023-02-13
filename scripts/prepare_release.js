const fs = require('fs');

const releaseDir = './app/build/outputs/apk/release';
const outputMetadata = JSON.parse(fs.readFileSync(`${releaseDir}/output-metadata.json`, 'utf8'));

const elements = outputMetadata.elements;

for (let element of elements) {
    let sourceApk = `${releaseDir}/${element.outputFile}`;
    let destinationApk = `${releaseDir}/fdroid-app-${element.versionCode}.apk`;
    fs.copyFileSync(sourceApk, destinationApk);
}
