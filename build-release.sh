#!/bin/bash


PRO_ROOT=$(pwd)

mvn clean

UI_ROOT=$PRO_ROOT/ui

cd $UI_ROOT

# Define the folder path
FOLDER="$PRO_ROOT/src/main/resources/static"
DIST="$UI_ROOT/dist"

# Check if the folder exists
if [ -d "$FOLDER" ]; then
  # Remove the folder if it exists
  rm -rf "$FOLDER"
  echo "Folder $FOLDER removed."
else
  echo "Folder $FOLDER does not exist."
fi

# Check if the folder exists
if [ -d "$DIST" ]; then
  # Remove the folder if it exists
  rm -rf "$DIST"
  echo "Folder $DIST removed."
else
  echo "Folder $DIST does not exist."
fi

npm run build:pro

# Define the source and destination paths
SOURCE="$UI_ROOT/dist"
DESTINATION="$PRO_ROOT/src/main/resources/static"

# Copy the folder to the destination
cp -r "$SOURCE" "$DESTINATION"

cd $PRO_ROOT

mvn package -DskipTests=true
