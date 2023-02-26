const fs = require('fs');
const assert = require('assert');

const releaseDir = './app/build/outputs/apk/release';
const outputMetadata = JSON.parse(fs.readFileSync(`${releaseDir}/output-metadata.json`, 'utf8'));

const elements = outputMetadata.elements;

for (let element of elements) {
    let sourceApk = `${releaseDir}/${element.outputFile}`;
    let abiFilter = element.filters[0];
    assert.equal("ABI", abiFilter.filterType);
    let abi = abiFilter.value;
    let destinationApk = `${releaseDir}/ruslin-${abi}-${element.versionName}-${outputMetadata.variantName}.apk`;
    fs.renameSync(sourceApk, destinationApk);
}
