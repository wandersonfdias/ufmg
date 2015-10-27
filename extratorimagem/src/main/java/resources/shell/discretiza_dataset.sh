INPUT_DATASET="$1"
OUTPUT_DATASET="$2"
WEKA_PATH="$3"
MEMORY="$4"

if [ $# -lt 4 ]; then
    echo "use: sh discretiza_dataset <input_dataset> <output_dataset> <weka_path> <memory_weka (ex: XG or Xm)>"
    exit
fi

WEKA_HOME=${WEKA_PATH}
export CLASSPATH="${WEKA_HOME}/weka.jar"

echo "Running..."
java -Xmx"${MEMORY}" weka.filters.unsupervised.attribute.Discretize -B 10 -M -1.0 -R first-last -i "${INPUT_DATASET}" -o "${OUTPUT_DATASET}"

echo "Done"
