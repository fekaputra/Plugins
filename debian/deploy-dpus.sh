#!/bin/bash

URL="http://127.0.0.1:28080/master/api/1/import/dpu/jar"
MASTER_USER=master
MASTER_PASS=commander

echo "---------------------------------------------------------------------"
echo "Installing DPUs.."
echo "..target instance: $URL"
echo "---------------------------------------------------------------------"

install_dpu() {
    dpu_file=$(ls $1)

    echo -n "..installing $dpu_file: "
    outputfile="/tmp/dpu_out.out"

    # fire cURL and wait until it finishes
    curl --user $MASTER_USER:$MASTER_PASS --fail --silent --output $outputfile -X POST -H "Cache-Control: no-cache" -H "Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW" -F file=@$dpu_file $URL?force=true 
    wait $!

    # check if the installation went well
    outcontents=`cat $outputfile`
    echo $outcontents
}

install_dpu "/usr/share/unifiedviews/dist/plugins/uv-e-distributionMetadata-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-e-executeShellScript-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-e-filesDownload-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-e-httpRequest-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-e-relationalFromSql-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-e-silkLinker-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-e-sparqlEndpoint-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-l-filesToParliament-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-l-filesToVirtuoso-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-l-filesUpload-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-l-rdfToVirtuoso-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-l-relationalToSql-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-excelToCsv-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-filesFilter-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-filesFindAndReplace-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-filesMerger-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-filesRenamer-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-filesToRdf-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-filterValidXml-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-fusionTool-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-gunzipper-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-gzipper-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-metadata-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-rdfMerger-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-rdfGraphMerger-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-rdfToFiles-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-rdfValidator-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-relational-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-relationalToRdf-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-sparqlConstruct-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-sparqlSelect-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-sparqlUpdate-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-tabular-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-tabularToRelational-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-unzipper-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-xslt-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-t-zipper-*.jar"
install_dpu "/usr/share/unifiedviews/dist/plugins/uv-q-sparqlAsk-*.jar"
