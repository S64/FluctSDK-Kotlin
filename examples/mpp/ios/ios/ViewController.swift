//
//  ViewController.swift
//  ios
//
//  Created by Shuma Yoshioka on 2019/05/05.
//  Copyright © 2019年 mpp.myfluctapp.example.kotlin.s64.jp. All rights reserved.
//

import UIKit
import mpp

class ViewController: UIViewController {

    class MyProxyDelegateImpl: MyProxyDelegate {
        
        let showButton: UIButton
        
        init(showButton: UIButton) {
            self.showButton = showButton
        }
        
        func onEvent(msg: String) {
            NSLog(msg)
        }
        
        func onError(msg: String) {
            NSLog(msg)
        }
        
        func onFinish() {
            showButton.isEnabled = true
        }
        
    }
    
    var mppProxy: MyProxy!
    
    @IBOutlet var showButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        mppProxy = MyProxy.init(listener: MyProxyDelegateImpl.init(showButton: showButton))
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    @IBAction func clickShowButton(_ sender: Any) {
        showButton.isEnabled = false
        mppProxy.loadAndShowRv(context: self)
    }
    
}

