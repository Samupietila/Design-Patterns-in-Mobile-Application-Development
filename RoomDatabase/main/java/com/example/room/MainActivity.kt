package com.example.room

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Space
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import com.example.room.ui.theme.RoomTheme
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.internal.synchronized
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private val viewModel: FoodViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoomTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "mainScreen") {
                    composable("mainScreen") { MainNav(navController) }
                    composable("RecipeScreen") { RecipeList(viewModel) }
                    composable("IngredientScreen") { IngredientList(viewModel) }
                }

                }
            }
        }
}

class FoodViewModel(application: Application): AndroidViewModel(application) {
    private val repository: Repository
    init {
        val dao = FoodDatabase.getInstance(application).FoodDao
        repository = Repository(dao)
    }
    fun getRecipesWithIngredients(): Flow<List<RecipeWithIngredients>> {
        return repository.getRecipes()
    }

    fun getIngredientsWithRecipes(): Flow<List<IngredientsWithRecipe>> {
        return repository.getIngredients()
    }

    suspend fun addRecipe(recipe: Recipe){
        repository.addRecipe(recipe)
    }
    suspend fun addIngredient(ingredient: Ingredient){
        repository.addIngredient(ingredient)
    }
    suspend fun addRecipeIngredientCrossRef(crossRef: RecipeIngredientCrossRef){
        repository.addRecipeIngredientCrossRef(crossRef)
    }

    }
@Composable
fun MainNav(navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("CHOOSE NOW!")
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            navController.navigate("RecipeScreen")
        }) {
            Text("Recipes")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navController.navigate("IngredientScreen")
        }) {
            Text("Ingredients")
        }

    }
}


    @Composable
    fun IngredientList(viewModel: FoodViewModel) {
        val ingredientList =
            viewModel.getIngredientsWithRecipes().collectAsState(initial = emptyList())
        var name by remember { mutableStateOf("") }
        var quantity by remember { mutableStateOf("") }
        var unit by remember { mutableStateOf("") }
        var recipeId by remember { mutableStateOf("") }
        val coroutineScope = rememberCoroutineScope()

        Column(modifier = Modifier.padding(16.dp)) {
            Text("Add Ingredient", modifier = Modifier.padding(bottom = 8.dp))
            TextField(
                value = recipeId,
                onValueChange = { recipeId = it },
                label = { Text("Recipe Id") },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Ingredient Name") },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Ingredient Quantity") },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TextField(
                value = unit,
                onValueChange = { unit = it },
                label = { Text("Unit Name") },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Button(onClick = {
                if (name.isNotBlank() && quantity.isNotBlank() && unit.isNotBlank() && recipeId.isNotBlank()) {
                    val ingredient = Ingredient(
                        recipeId = recipeId.toInt(),
                        name = name,
                        quantity = quantity.toDouble(),
                        unit = unit
                    )
                    coroutineScope.launch {
                        viewModel.addIngredient(ingredient)
                        val crossRef = RecipeIngredientCrossRef(
                            recipeId = recipeId.toInt(),
                            ingredientId = ingredient.ingredientId
                        )
                        viewModel.addRecipeIngredientCrossRef(crossRef)
                    }
                }
            }) { Text("Add Recipe") }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(ingredientList.value) { ingredient ->
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Ingredient: ${ingredient.ingredient.name} ${ingredient.ingredient.ingredientId}")
                        Text("in these recipes:")
                        ingredient.recipes.forEach { recipe ->
                            Text("- ${recipe.name} (${recipe.country})")
                        }

                    }

                }
            }
        }
    }


@Composable
fun RecipeList(viewModel: FoodViewModel) {
    val recipeList = viewModel.getRecipesWithIngredients().collectAsState(initial = emptyList())
    var name by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Add Recipe", modifier = Modifier.padding(bottom = 8.dp))
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Recipe Name") },
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TextField(
            value = country,
            onValueChange = { country = it },
            label = { Text("Country") },
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Button(onClick = {
            if (name.isNotBlank() && country.isNotBlank()) {
                val recipe = Recipe(name = name, country = country)
                coroutineScope.launch {
                    viewModel.addRecipe(recipe)
                }
            }
        }) { Text("Add Recipe") }


        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(recipeList.value) { recipeWithIngredients ->
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Recipe: ${recipeWithIngredients.recipe.name} ${recipeWithIngredients.recipe.recipeId}")
                    Text("Country: ${recipeWithIngredients.recipe.country}")
                    Text("Ingredients:")
                    recipeWithIngredients.ingredients.forEach { ingredient ->
                        Text("- ${ingredient.name} (${ingredient.quantity} ${ingredient.unit})")
                    }
                }
            }
        }
    }

}


@Entity(tableName = "Recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val recipeId: Int = 0,
    val name: String,
    val country: String,
    )

@Entity(tableName = "Ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val ingredientId: Int = 0,
    val name: String,
    val recipeId: Int,
    val quantity: Double,
    val unit: String,
)
@Entity(primaryKeys = ["recipeId", "ingredientId"])
data class RecipeIngredientCrossRef(
    val recipeId: Int,
    val ingredientId: Int
)
data class RecipeWithIngredients(
    @Embedded val recipe: Recipe,
    @Relation(
        parentColumn = "recipeId",
        entityColumn = "ingredientId",
        associateBy = Junction(RecipeIngredientCrossRef::class)
    )
    val ingredients: List<Ingredient>
)
data class IngredientsWithRecipe(
    @Embedded val ingredient: Ingredient,
    @Relation(
        parentColumn = "ingredientId",
        entityColumn = "recipeId",
        associateBy = Junction(RecipeIngredientCrossRef::class)
    )
    val recipes: List<Recipe>
)

class Repository(private val foodDao: RecipeIngredientDao) {
    fun getRecipes() : Flow<List<RecipeWithIngredients>> = foodDao.getRecipesWithIngredients()
    fun getIngredients() : Flow<List<IngredientsWithRecipe>> = foodDao.getIngredientsWithRecipes()
    suspend fun addRecipe(recipe: Recipe) = foodDao.insertRecipe(recipe)
    suspend fun updateRecipe(recipe: Recipe) = foodDao.updateRecipe(recipe)
    suspend fun deleteRecipe(recipe: Recipe) = foodDao.deleteRecipe(recipe)
    suspend fun addIngredient(ingredient: Ingredient) = foodDao.insertIngredient(ingredient)
    suspend fun updateIngredient(ingredient: Ingredient) = foodDao.updateIngredient(ingredient)
    suspend fun deleteIngredient(ingredient: Ingredient) = foodDao.deleteIngredient(ingredient)
    suspend fun addRecipeIngredientCrossRef(crossRef: RecipeIngredientCrossRef) = foodDao.insertRecipeIngredientCrossRef(crossRef)
}

@Dao
interface RecipeIngredientDao{
    @Transaction
    @Query("SELECT * from Recipes")
   fun getRecipesWithIngredients(): Flow<List<RecipeWithIngredients>>

    @Transaction
    @Query("SELECT * from Ingredients")
    fun getIngredientsWithRecipes(): Flow<List<IngredientsWithRecipe>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipe(recipe: Recipe)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIngredient(ingredient: Ingredient)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipeIngredientCrossRef(crossRef: RecipeIngredientCrossRef)

    @Update
    suspend fun updateRecipe(recipe: Recipe)
    @Update
    suspend fun updateIngredient(ingredient: Ingredient)
    @Delete
    suspend fun deleteRecipe(recipe: Recipe)
    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)
}

@Database(entities = [Recipe::class, Ingredient::class, RecipeIngredientCrossRef::class], version = 4, exportSchema = false)
abstract class FoodDatabase: RoomDatabase() {
    abstract val FoodDao: RecipeIngredientDao
    companion object {
        @Volatile
        private var INSTANCE: FoodDatabase? = null
        @OptIn(InternalCoroutinesApi::class)
        fun getInstance(context: Context): FoodDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if(instance == null) {
                    instance = Room.databaseBuilder(context,
                        FoodDatabase::class.java, "food_database")
                        .fallbackToDestructiveMigration().build()
                }
                return instance
            }
        }
    }
}