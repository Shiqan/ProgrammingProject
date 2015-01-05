ProgrammingProject
==================

###### Ferron Saan
###### 10386831

> A simple arcade jumper game created with [AndEngine](https://github.com/nicolasgramlich/AndEngine).

### Preliminary sketch
##### MenuScene
![MenuScene](https://raw.githubusercontent.com/Shiqan/ProgrammingProject/master/MenuScene.PNG)

##### GameScene
![GameScene](https://raw.githubusercontent.com/Shiqan/ProgrammingProject/master/GameScene.PNG)

##### OptionsScene
![OptionsScene](https://raw.githubusercontent.com/Shiqan/ProgrammingProject/master/OptionsScene.PNG)

### Decomposing the problem
The application can be decomposed into three main parts:
* ResourceManager (handling loading/unloading of the resources like images, sounds, fonts etc).
* SceneManager (handling switching of scenes).
* Scenes (MenuScene, OptionsScene, GameScene).

The GameScene can also be decomposed into three main parts:
* PhysicsWorld
* Player
* GameObjects

The player and the different gameobjects will be loaded into the physicsworld where the physics of this world will be applied (like gravity). 

### APIs
AndEngine GLES 2 branch and the AndEnginePhysicsBox2DExtension.

### Potential problems
A potential issue is the limited documentation of AndEngine.

### Similar applications
* The impossible game
* Geometry Dash
