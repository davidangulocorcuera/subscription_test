package davidangulo.subscription

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponse
import com.android.billingclient.api.BillingClient.SkuType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetailsParams
import com.example.davidangulo.subscription.R
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var mBillingClient: BillingClient
    private var skuList: ArrayList<String> = ArrayList()
    private var base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAm+JeMkAOzWyRJZZOQIQ/yZovy0sKVLJ/DMhvhjxcI9dO89lqPzAFT5cMTDswEus32fERXudSMVZJyscjpscQjtmprlM3Q4NpUDgeSYnN1zLWH1GKr2Ci/nYFJc3aHVTwl0ZoOo50GdvfC+Av2qO1BFa3EW9qbCALJt32IMTGcQWzZz0BN1zBqrUj78Q5wH9H1vz5KmMlAM+rEDQKwyFgOfhYPxlK+8K+SqNHRcTmf2q5bqgKkxZfKANujzIQAjbRJZQvoFRsuL2ko7tOQqcdNIogVbavpvWZhe0/8FFai8Qc+I1I3+kRIO/Ra0KahpmRbHaTCKs9sdLQnHUPBLa3kQIDAQAB"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setButtonListener()
        initializeBillingClient()
        startConectionBillingClient()

        skuList.add("product_1")
        skuList.add("product_2")

        val params = SkuDetailsParams.newBuilder()

        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        mBillingClient.querySkuDetailsAsync(
            params.build()
        ) { responseCode, skuDetailsList ->
            if (responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null) {
                for (skuDetails in skuDetailsList) {
                    val sku = skuDetails.sku
                    val price = skuDetails.price
                    if ("product_1" == sku) {
                        Toast.makeText(this@MainActivity, "Product 1", Toast.LENGTH_SHORT).show()
                    } else if ("product_2" == sku) {
                        Toast.makeText(this@MainActivity, "Product 2", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    }

    fun initializeBillingClient() {
        mBillingClient =
            BillingClient.newBuilder(this).setListener(PurchasesUpdatedListener { responseCode, purchases ->
                if (purchases != null){
                    for (purchase in purchases) {
                        // When every a new purchase is made
                        // Here we verify our purchase
                        if (!verifyValidSignature(purchase.originalJson, purchase.signature)) {
                            // Invalid purchase
                            // show error to user
                            Log.i(Security.TAG, "Got a purchase: $purchase; but signature is bad. Skipping...")
                            return@PurchasesUpdatedListener
                        } else {
                            // purchase is valid
                            // Perform actions

                        }
                    }
                }
            }).build()
    }
    private fun verifyValidSignature(signedData: String, signature: String): Boolean {
        return try {
            Security.verifyPurchase(
                base64Key,
                signedData,
                signature
            )
        } catch (e: IOException) {
            Log.e(Security.TAG, "Got an exception trying to validate a purchase: $e")
            false
        }

    }

    fun startConectionBillingClient() {
        mBillingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(@BillingClient.BillingResponse billingResponseCode: Int) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    // The billing client is ready. You can query purchases here.
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    // Must return purchased list
    fun purchasedItemslist() {
        mBillingClient.queryPurchaseHistoryAsync(
            SkuType.INAPP
        ) { responseCode, purchasesList ->
            if (responseCode == BillingResponse.OK && purchasesList != null) {
                for (purchase in purchasesList) {
                    // Process the result.
                }
            }
        }
    }

    fun purchaseProduct() {

        val flowParams = BillingFlowParams.newBuilder()
            .setSku("product_1")
            .build()
        val responseCode = mBillingClient.launchBillingFlow(this, flowParams)
    }

    fun purchaseSubscription() {
        val builder = BillingFlowParams.newBuilder()
            .setSku("sub_1").setType(BillingClient.SkuType.SUBS)
        val responseCode = mBillingClient.launchBillingFlow(this, builder.build())
    }

    fun setButtonListener() {
        btn_buy.setOnClickListener {
            purchaseProduct()
            initializeBillingClient()
        }
        btn_subscription.setOnClickListener {
            purchaseSubscription()
        }
    }
}
