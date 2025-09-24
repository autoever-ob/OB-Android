package com.nobody.campick.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.nobody.campick.R
import com.nobody.campick.adapters.ProfileProductAdapter
import com.nobody.campick.databinding.ActivityMyProductsBinding
import com.nobody.campick.managers.UserState
import com.nobody.campick.viewmodels.MyProductsViewModel
import com.nobody.campick.views.components.CommonHeader
import kotlinx.coroutines.launch

class MyProductsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyProductsBinding
    private val viewModel: MyProductsViewModel by viewModels()
    private lateinit var adapter: ProfileProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val memberId = intent.getStringExtra("memberId") ?: UserState.memberId.value

        setupHeader()
        setupRecyclerView()
        observeViewModel()

        viewModel.loadProducts(memberId)
    }

    private fun setupHeader() {
        binding.commonHeader.setupHeader(
            type = CommonHeader.HeaderType.Navigation(
                title = "내 매물",
                showBackButton = true,
                showRightButton = false
            ),
            onBackClick = { finish() }
        )
    }

    private fun setupRecyclerView() {
        adapter = ProfileProductAdapter(
            onProductClick = { product ->
                val intent = VehicleDetailActivity.newIntent(
                    context = this,
                    vehicleId = product.productId.toString()
                )
                startActivity(intent)
            },
            onLoadMore = {
                // TODO: Load more products
            }
        )

        binding.recyclerViewProducts.apply {
            layoutManager = GridLayoutManager(this@MyProductsActivity, 1)
            adapter = this@MyProductsActivity.adapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.vehicles.collect { vehicles ->
                adapter.submitList(vehicles)
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                // TODO: Show/hide loading indicator
            }
        }

        lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                // TODO: Show error message
            }
        }
    }
}