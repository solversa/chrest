//TODO: Tidy up all comments before git commit and push to online CHREST repo

package jchrest.architecture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Class that represents the "Mind's Eye".  
 * 
 * The mind's eye is a finite-sized, 2D ArrayList that represents the vision of 
 * the observer whose CHREST model is associated with a mind's eye instance.  
 * Information in the mind's eye can be manipulated independently of the
 * environment that the observer is currently situated in to test outcomes of 
 * actions without incurring these outcomes in "reality".
 * 
 * The limits of the mind's eye are determined by the initial vision used to
 * construct an instance of this class i.e. if the observer can see 4 squares 
 * north and 3 squares east/west, the mind's eye created will not be able to 
 * represent objects that are 5 squares north or 4 squares east/west.
 * 
 * @author Martyn Lloyd-Kelly <martynlk@liverpool.ac.uk>
 */
public class MindsEye {
  
  //Time taken (ms) to access the mind's eye.
  private final int _accessTime;
  
  //The key-value pairings to enable resolution of domain-specific to minds' eye 
  //coordinates. Key-values are strings since two integers need to be combined
  //for both keys and values.
  private final HashMap<String, String> _domainSpecificToMindsEyeCoordMappings;
  
  //The key-value pairings to enable resolution of mind's eye coordinates to 
  //domain-specific coordinates. Key-values are strings since two integers need 
  //to be combined for both keys and values.
  private final HashMap<String, String> _mindsEyeToDomainSpecificCoordMappings;
  
   //The CHREST model that a mind's eye instance is associated with.
  private final Chrest _model;
  
  //Time taken (ms) to move an object in the mind's eye.
  private final int _movementTime;
  
  //The mind's eye data structure.
  private ArrayList<ArrayList> _visualSpatialField;
  
  /**
   * Constructor for "MindsEye" object.
   * 
   * @param model The CHREST model instance that the mind's eye instance is 
   * associated with.
   * 
   * @param vision An array consisting of object identifiers and domain-specific 
   * row/column coordinates separated by commas i.e. "PersonA,3,2" would 
   * indicate that the "PersonA" object can be seen at row 3 and column 2.
   * <ul>
   *  <li>
   *    When translating the visual information passed, the constructor will 
   *    calculate the maximum number of visual units that can be seen to the
   *    east/west and will assume that, for every visual unit that can be seen 
   *    north/south, the observer can see this maximum number of units 
   *    east/west.  
   *  </li>
   *  <li>
   *    As a consequence of the point above, ensure that all objects that can be
   *    "seen" have a unique <b>object identifier</b> set (uniqueness of object 
   *    identifiers is required due to the functionality of the "moveObjects" 
   *    function (see {@link#moveObjects(java.lang.String[][])} for details). 
   *    If a coordinate in the domain that can be seen contains no object, pass 
   *    an empty string as an object identifier otherwise the coordinate will be 
   *    interpreted as a "blind spot" and will contain a null value.  Thus, any 
   *    object moved to such a coordinate in future will be considered as 
   *    "lost".
   *  </li>
   *  <li>
   *    Note that domain coordinates <b>must</b> be integers; fractional numbers
   *    can not be used.  Therefore, if the domain consists of a 2D grid of 
   *    squares and it is possible for objects to move along half a square, 
   *    please ensure that the domain-specific coordinates are translated into 
   *    integers accordingly.  For example, if the agent can see 2 squares to 
   *    the east and west (resulting in 4 row elements) but objects can be moved 
   *    along half a square then round .5 coordinates up to the nearest unit 
   *    (resulting in 8 row elements).
   *  </li>
   *  <li>
   *    Note that coordinates can be absolute or relative to the observer when 
   *    the mind's eye is created however, all subsequent operations in the 
   *    mind's eye will be performed in this context.
   *  </li>
   * </ul>
   * 
   * @param accessTime The time taken (in milliseconds) to access the mind's eye 
   * when the "moveObjects" function is used.
   * 
   * @param movementTime The time taken (in milliseconds) to move an object 
   * in the mind's eye.
   *
   *  TODO: Have tried to remove null elements from visualSpatialField and 
   *        corresponding coordinate mappings however, it is extremely difficult
   *        to determine how to reorder mappings after null elements have been
   *        removed.
   *  TODO: What about objects that take up more than one "unit"?
   */
  public MindsEye(Chrest model, String [] vision, int accessTime, int movementTime){
    
    this._model = model;
    this._accessTime = accessTime;
    this._movementTime = movementTime;
    this._visualSpatialField = new ArrayList<>();
    this._domainSpecificToMindsEyeCoordMappings = new HashMap<>();
    this._mindsEyeToDomainSpecificCoordMappings = new HashMap<>();
    
    //Used to determine sizes of ArrayList dimensions that consitute the 
    //"_visualSpatialField" data structure.
    int minDomainSpecificRowCoord = 0;
    int maxDomainSpecificRowCoord = 0;
    int minDomainSpecificColCoord = 0;
    int maxDomainSpecificColCoord = 0;
    
    //Cycle through "vision" array elements.
    for(int i = 0; i < vision.length; i++){
      
      //Extract domain-specific row and col coordinates for the current element.       
      String[] objectInfo = processObjectInfo(vision[i]);
      int domainSpecificRowCoord = Integer.valueOf(objectInfo[1]);
      int domainSpecificColCoord = Integer.valueOf(objectInfo[2]);
      
      //Check to see if this is the first element of the "vision" array.  If it
      //is, set the min/max row/col variables to the row/col coordinates of this
      //first element so that subsequent "vision" element row/col coordinates 
      //can be compared. 
      if(i == 0){
        minDomainSpecificRowCoord = domainSpecificRowCoord;
        maxDomainSpecificRowCoord = domainSpecificRowCoord;
        minDomainSpecificColCoord = domainSpecificColCoord;
        maxDomainSpecificColCoord = domainSpecificColCoord;
      }
      //Otherwise, compare the "vision" array element's row/col coordinates 
      //against the max/min row/col values and overwrite these values 
      //accordingly.
      else{
        if(domainSpecificRowCoord < minDomainSpecificRowCoord){
          minDomainSpecificRowCoord = domainSpecificRowCoord;
        }
        
        if(domainSpecificRowCoord > maxDomainSpecificRowCoord){
          maxDomainSpecificRowCoord = domainSpecificRowCoord;
        }
        
        if(domainSpecificColCoord < minDomainSpecificColCoord){
          minDomainSpecificColCoord = domainSpecificColCoord;
        }
        
        if(domainSpecificColCoord > maxDomainSpecificColCoord){
          maxDomainSpecificColCoord = domainSpecificColCoord;
        }
      }
    }
    
    //Calculate the size of the 1st and 2nd dimension arrays for the instance's 
    //"_visualSpatialField" by taking the difference between the max /min 
    //domain-specific row/column values and adding 1.  Adding 1 ensures that 
    //there is an element for the minimum row/col value otherwise, if the 
    //min/max row values are set to 1 and 5, the mind's eye would only create 4 
    //rows so row 1/5 in "reality" would not be represented in the mind's eye.
    int numberOfRows = (maxDomainSpecificRowCoord - minDomainSpecificRowCoord) + 1;
    int numberOfCols = (maxDomainSpecificColCoord - minDomainSpecificColCoord) + 1;
    
    //Generate the domain-specific to mind's eye coordinate mappings.
    for(int domainRow = minDomainSpecificRowCoord, genericRow = 0; genericRow < numberOfRows; domainRow++, genericRow++){
      for(int domainCol = minDomainSpecificColCoord, genericCol = 0; genericCol < numberOfCols; domainCol++, genericCol++){  
        String domainRowAndCol = String.valueOf(domainRow) + "," + String.valueOf(domainCol);
        String genericRowAndCol = String.valueOf(genericRow) + "," + String.valueOf(genericCol);
        this._domainSpecificToMindsEyeCoordMappings.put(domainRowAndCol, genericRowAndCol);
        this._mindsEyeToDomainSpecificCoordMappings.put(genericRowAndCol, domainRowAndCol);
      }
    }
    
    //Instantiate the "_visualSpatialField" with null values for the max number 
    //of row/col elements determined above.
    for(int row = 0; row < numberOfRows; row++){
      ArrayList<String> colSpace = new ArrayList<>();
      this._visualSpatialField.add(colSpace);
      for(int col = 0; col < numberOfCols; col++){
        this._visualSpatialField.get(row).add(null);
      }
    }
    
    //Populate "_visualSpatialField" with the objects at the coordinates 
    //specified in the "vision" string array passed.
    for(String objectInfo : vision){
      String[] objectInfoParts = this.processObjectInfo(objectInfo);
      int[] nonDomainSpecificCoords = resolveDomainSpecificCoord(Integer.valueOf(objectInfoParts[1]), Integer.valueOf(objectInfoParts[2]) );
      this._visualSpatialField.get( nonDomainSpecificCoords[0] ).set(nonDomainSpecificCoords[1], objectInfoParts[0]);
    }
  }
  
  /**
   * Retrieves the entire contents of the mind's eye along in relation to domain
   * specific coordinates.
   * 
   * @return An ArrayList of strings containing the contents of each mind's eye
   * coordinate along with the relevant domain-specific row and column 
   * coordinates.  Object, row and column information are separated by a 
   * semi-colon (;) and so can be used as a delimiter to split information.
   * Commas (,) are used to separate object identifiers in mind's eye content so 
   * can be used as a delimiter to extract content information.
   */
  public ArrayList<String> getMindsEyeContentSpecificToDomain(){
    ArrayList<String> domainSpecificContentsOfMindsEye = new ArrayList<>();
    for(int row = 0; row < this._visualSpatialField.size(); row++){
      ArrayList rowArray = this._visualSpatialField.get(row);
      for(int col = 0; col < rowArray.size(); col++){
        String[] domainSpecificRowAndColCoordinates = this._mindsEyeToDomainSpecificCoordMappings.get(Integer.toString(row) + "," + Integer.toString(col)).split(",");
        String domainSpecificRowString = domainSpecificRowAndColCoordinates[0];
        String domainSpecificColString = domainSpecificRowAndColCoordinates[1];
        String contents = this.getMindsEyeContentUsingDomainSpecificCoords(Integer.valueOf(domainSpecificRowString), Integer.valueOf(domainSpecificColString));
        domainSpecificContentsOfMindsEye.add(contents + ";" + domainSpecificRowString + ";" + domainSpecificColString);
      }
    }
    
    return domainSpecificContentsOfMindsEye;
  }
  
  /**
   * Takes domain-specific row and column coordinates and returns the object 
   * identifier found at the resolved mind's eye row and column coordinates.
   * 
   * @param domainSpecificRow
   * @param domainSpecificCol
   * @return 
   */
  public String getMindsEyeContentUsingDomainSpecificCoords(int domainSpecificRow, int domainSpecificCol){
    int[] nonDomainSpecificCoords = this.resolveDomainSpecificCoord(domainSpecificRow, domainSpecificCol);
    return String.valueOf(this._visualSpatialField.get( nonDomainSpecificCoords[0] ).get( nonDomainSpecificCoords[1] ));
  }
  
  /**
   * Moves objects in the mind's eye according to the sequence of moves passed
   * in as a parameter to this function and if moves are successful, the clock
   * of the CHREST model associated with this mind's eye will be advanced by the
   * result of: _accessTime + (_movementTime * number of moves performed in 
   * total).
   * 
   * There are a number of caveats to take note of when using this function:
   * <ol type="1">
   *  <li>
   *    Object movement is performed serially i.e. one object at a time.
   *  </li>
   *  <li>
   *    When moving an object, the first move should stipulate where the object 
   *    is currently located in the mind's eye (using domain-specific 
   *    coordinates).  If the object has previously been moved in the mind's eye 
   *    then the initial location passed for the object should be relative to 
   *    its current coordinates in the mind's eye.  If the object specified is 
   *    not found at the coordinates specified then the function will terminate.
   *  </li>
   *  <li>
   *    If no moves are specified for an object after initial identification, 
   *    the function will terminate.
   *  </li>
   *  <li>
   *    Due to the fact that object manipulation in the mind's eye is 
   *    essentially string manipulation, the "String.replaceAll()" function is 
   *    used to edit mind's eye coordinate contents when an object is moved 
   *    <i>from</i> them.  Consequently, object identifiers in the move sequence 
   *    should be <b>unique</b> otherwise, given two identical object 
   *    identifiers on the same mind's eye coordinates, moving one of them will 
   *    cause both objects to be removed from the coordinates.
   *  </li>
   *  <li>
   *    If an an object is moved to mind's eye coordinates that are already 
   *    occupied then the two objects will co-exist on the coordinates; the 
   *    new object does not overwrite the old object.
   *  </li>
   *  <li>
   *    If an object is moved to coordinates not represented in the mind's eye
   *    then further manipulation of this object will not occur but the function
   *    will continue to execute.
   *  </li>
   * </ol>
   * 
   * 
   * @param domainSpecificMoves A 2D array whose first dimension elements 
   * should contain arrays whose elements prescribe a sequence of moves for one 
   * object by specifying domain-specific coordinates.  For example, if two 
   * objects, A and B, are to be moved from domain specific coordinates 0, 1 and 
   * 0, 2 to 1, 1 and 1, 2 respectively, the array passed should be: 
   * [ ["A,0,1", "A,1,1"], ["B,0,2","B,1,2"] ].  See the discussion of the 
   * function's caveats above for further implementation and usage details of 
   * this parameter.
   * 
   * @return An array whose first element contains a boolean value indicating
   * whether the moves contained in the "domainSpecificMoves" parameter were
   * executed successfully in the mind's eye or not.  The second element of the
   * array contains any error messages that may have been generated during 
   * execution of the moves in "domainSpecificMoves".  Note that the second 
   * array element will always be empty if the moves in "domainSpecificMoves"
   * were executed successfully and not empty otherwise.
   */
  public Object[] moveObjects(String[][] domainSpecificMoves){
    
    //Counter for how many moves have been applied - acts as a multiplier for 
    //the "_movementTime" parameter multiplicand so that the clock of the CHREST
    //model associated with this mind's eye can be incremented correctly.
    int movesApplied = 0;
    
    //Indicates whether objects have all been moved successfully.
    boolean moveObjectsSuccessful = true;
    
    //Holds any error messages to be returned.
    String errorMessage = "";
    
    //Copy the current contents of "_visualSpatialField" before any moves are
    //applied so that if any object's move is illegal, all changes made to 
    //"_visualSpatialField" up until the illegal move can be reversed.
    ArrayList<ArrayList> visualSpatialFieldBeforeMovesApplied = new ArrayList<>();
    for(int i = 0; i < this._visualSpatialField.size(); i++){
      visualSpatialFieldBeforeMovesApplied.add(new ArrayList<>());
      for(int j = 0; j< this._visualSpatialField.get(i).size(); j++){
        visualSpatialFieldBeforeMovesApplied.get(i).add( String.valueOf( this._visualSpatialField.get(i).get(j) ) );
      }
    }
    
    //Process each object's move sequence.
    for(int object = 0; object < domainSpecificMoves.length; object++){
      
      //Check to see if at least one move has been specified for an object along
      //with information regarding its current location in the domain.
      if(domainSpecificMoves[object].length >= 2){
      
        //Extract the initial information for the object
        String[] initialDomainSpecificObjectInformation = this.processObjectInfo(domainSpecificMoves[object][0]);
        String initialObjectIdentifier = initialDomainSpecificObjectInformation[0];
        int currentDomainSpecificRow = Integer.valueOf(initialDomainSpecificObjectInformation[1]);
        int currentDomainSpecificCol = Integer.valueOf(initialDomainSpecificObjectInformation[2]);

        int[] initialMindsEyeCoords = this.resolveDomainSpecificCoord(currentDomainSpecificRow, currentDomainSpecificCol);
        int currentMindsEyeRow = initialMindsEyeCoords[0];
        int currentMindsEyeCol = initialMindsEyeCoords[1];

        //Check to see if the object specified in reltion to domain-specific 
        //coordinates is located at the relevant mind's eye coordinates.
        String contentsOfInitialCoordinatesSpecifiedInMindsEye = this.getMindsEyeContentUsingDomainSpecificCoords(currentDomainSpecificRow, currentDomainSpecificCol);
        if(contentsOfInitialCoordinatesSpecifiedInMindsEye.contains(initialObjectIdentifier)){

          //Process each move for this object starting from the first element of 
          //the current second dimension array.
          for(int move = 1; move < domainSpecificMoves[object].length; move++){

            //Extract domain specific move information.
            String[] domainSpecificMoveInformation = this.processObjectInfo(domainSpecificMoves[object][move]);
            String objectToBeMoved = domainSpecificMoveInformation[0];
            
            //Check to see if the object being moved is the object originally 
            //specified in the first element of the move sequence.
            if( initialObjectIdentifier.equals(objectToBeMoved)){
              
              //Check to see if the object to be moved still exists in the 
              //"_visualSpatialField" of the mind's eye since a previous move 
              //may have caused it to be moved to coordinates outside the 
              //range of the mind's eye's "_visualSpatialField".
              boolean objectExistsInMindsEye = false;
              for(int row = 0; row < this._visualSpatialField.size(); row++){
                for(int col = 0; col < this._visualSpatialField.get(row).size(); col++){
                  String columnContents = String.valueOf(this._visualSpatialField.get(row).get(col));
                  if(columnContents.contains(objectToBeMoved)){
                    objectExistsInMindsEye = true;
                  }
                }
              }
              if(objectExistsInMindsEye){
                
                //At this point, all potential issues with the move have been 
                //checked so nothing can go wrong.  Consequently, increment the
                //"movesApplied" counter value by 1.
                movesApplied++;
                
                //Extract domain specific row/col to move to.
                int domainRowToMoveObjectTo = Integer.valueOf(domainSpecificMoveInformation[1]);
                int domainColToMoveObjectTo = Integer.valueOf(domainSpecificMoveInformation[2]);
                
                //Convert domain-specific coordinates to move to into their 
                //relevant mind's eye coordinates.
                int[] mindsEyeCoordsToMoveTo = this.resolveDomainSpecificCoord( domainRowToMoveObjectTo, domainColToMoveObjectTo);
                
                //Remove the object from its current coordinates in 
                //"_visualSpatialField" and tidy up any double/leading/trailing
                //commas in the contents of the mind's eye row/column that the
                //object has been moved from.
                String mindsEyeCurrentCoordsContentAfterObjectRemoval = this.getMindsEyeContentUsingDomainSpecificCoords( currentDomainSpecificRow, currentDomainSpecificCol ).replaceAll(initialObjectIdentifier, "").replaceAll(",,", ",").replaceAll("^,\\s*|,\\s*$", "");
                this._visualSpatialField.get(currentMindsEyeRow).set(currentMindsEyeCol, mindsEyeCurrentCoordsContentAfterObjectRemoval);
                
                //Check to see if the mind's eye coordinates that were resolved 
                //above are represented in the visual spatial field.  If they 
                //are, add the object identifier to the new mind's eye 
                //coordinates.
                if(mindsEyeCoordsToMoveTo != null){

                  //Extract mind's eye coordinates to move to using 
                  //domain-specific coordinates to move to.
                  int mindsEyeRowToMoveTo = mindsEyeCoordsToMoveTo[0];
                  int mindsEyeColToMoveTo = mindsEyeCoordsToMoveTo[1];
                  
                  //Get the current content of the mind's eye coordinates that 
                  //the object will be moved to.
                  String mindsEyeCoordsToMoveToContentBeforeObjectMovement = this.getMindsEyeContentUsingDomainSpecificCoords(domainRowToMoveObjectTo, domainColToMoveObjectTo);
                  
                  //Create a blank string to hold the new mind's eye coordinate
                  //contents that the object will be moved to.
                  String mindsEyeCoordsToMoveToContentAfterObjectMovement;

                  //Check to see if the mind's eye coordinates to move to 
                  //content is empty.  If so, simply overwrite the content with
                  //the object identifier in question otherwise, append the 
                  //object identifier in question to the current content of the 
                  //mind's eye coordinates to move to preceeded by a comma.
                  if(mindsEyeCoordsToMoveToContentBeforeObjectMovement.isEmpty()){
                    mindsEyeCoordsToMoveToContentAfterObjectMovement = objectToBeMoved;
                  }
                  else{
                    mindsEyeCoordsToMoveToContentAfterObjectMovement = mindsEyeCoordsToMoveToContentBeforeObjectMovement + "," + objectToBeMoved;
                  }

                  //Set the content of the mind's eye row/col to move to to the
                  //content specified above.
                  this._visualSpatialField.get(mindsEyeRowToMoveTo).set(mindsEyeColToMoveTo, mindsEyeCoordsToMoveToContentAfterObjectMovement);
                  
                  //Set the values of "currentMindsEyeRow" and 
                  //"currentMindsEyeCol" to the values of "mindsEyeRowToMoveTo"
                  //and "mindsEyeColToMoveTo" so that any subsequent moves for
                  //the object in this sequence will remove the object from the
                  //correct coordinates in the mind's eye.
                  currentMindsEyeRow = mindsEyeRowToMoveTo;
                  currentMindsEyeCol = mindsEyeColToMoveTo;
                }
              }
            }
            //The object being moved is not the original object specified.
            else{
              moveObjectsSuccessful = false;
              errorMessage = "Object " + objectToBeMoved + " is not the object initially specified for this move sequence: " + initialObjectIdentifier + ".";
              break;
            }

            //Check to see if the object's move sequence should be broken out 
            //of.
            if(!moveObjectsSuccessful){
              break;
            }
          }//End second dimension loop
        }//End check for initial object specification in initial coordinates.
        //Initial coordinates specified for the object are incorrect.
        else{
          moveObjectsSuccessful = false;
          errorMessage = "Object " + initialObjectIdentifier + " is not located at the initial domain-specific coordinates specified: " + currentDomainSpecificRow + ", " + currentDomainSpecificCol + " (translated mind's eye coordinates : " + currentMindsEyeRow + ", " + currentMindsEyeCol + ").";
        }
      }//End check for number of object moves being greater than or equal to 2.
      else{
        moveObjectsSuccessful = false;
        String[] objectInfo = this.processObjectInfo(domainSpecificMoves[object][0]);
        errorMessage = "The moves for object " + objectInfo[0] + " does not contain any moves after the current location of the object is specified: " + Arrays.toString(domainSpecificMoves[object]) + ".";
      }
      
      //Check to see if further object moves should be processed.
      if(!moveObjectsSuccessful){
        break;
      }
    }//End first dimension loop
    
    //If there was a problem with the object movement sequence specified, reset
    //the visual spatial field to the state it was in before any moves were
    //applied.  Otherwise, advance the clock of the CHREST model associated with
    //the mind's eye instance by the required time.
    if(!moveObjectsSuccessful){
      this._visualSpatialField = visualSpatialFieldBeforeMovesApplied;
    }
    else{
      this._model.advanceClock(this._accessTime + (movesApplied * this._movementTime));
    }
    
    //Set the values of the array to be returned and return the array.
    Object [] result = {moveObjectsSuccessful, errorMessage};
    return result;
  }
  
  /**
   * If standard object information is passed (object identifier, row and col) 
   * this function returns an array whose elements contain the distinct parts of
   * the object information passed in the order they are declared.
   * 
   * If the array of strings created from the object information passed is not 
   * of length 3 (indicating that all pieces of information required to perform 
   * a move are present) then an error will be thrown. 
   * 
   * @param objectInfo A string of object information to be processed. Distinct 
   * pieces of information (object identifier and coordinates, for example) 
   * should be separated by commas (,). 
   * 
   * @return A string array whose elements contain each of the distinct pieces 
   * of information passed in the "objectInfo" parameter in the order they occur
   * in "objectInfo".
   */
  private String[] processObjectInfo(String objectInfo){
    String[] partsOfObjectInfo = objectInfo.replaceAll("\\s", "").split(",");
    
    if(partsOfObjectInfo.length != 3){
      throw new Error("Object information should comprise 3 parts: object identifier, row and column seperated by commas (,) but " + objectInfo + " only contains " + partsOfObjectInfo.length + " parts.");
    }
    
    return partsOfObjectInfo;
  }
  
  /**
   * Returns the mind's eye coordinates mapped to the domain-specific 
   * coordinates passed.  Ensure that the coordinates passed are 
   * relative/absolute depending upon the coordinates used to instantiate the 
   * mind's eye instance. 
   * 
   * @param domainSpecificRow The domain-specific row to resolve.
   * @param domainSpecificCol The domain-specific col to resolve.
   * @return Two element integer array whose first element contains the resolved
   * mind's eye row coordinate and second elements contains the resolved mind's 
   * eye column coordinate.
   */
  private int[] resolveDomainSpecificCoord(int domainSpecificRow, int domainSpecificCol){
    String domainSpecificCoords = Integer.toString(domainSpecificRow) + "," + Integer.toString(domainSpecificCol);
    int[] mindsEyeCoords = null;
    
    if(this._domainSpecificToMindsEyeCoordMappings.containsKey(domainSpecificCoords)){
      String[] mindsEyeCoordsString = this._domainSpecificToMindsEyeCoordMappings.get( domainSpecificCoords ).split(",");
      mindsEyeCoords = new int[2];
      mindsEyeCoords[0] = Integer.valueOf(mindsEyeCoordsString[0]);
      mindsEyeCoords[1] = Integer.valueOf(mindsEyeCoordsString[1]);
    }
    
    return mindsEyeCoords;
  }
}