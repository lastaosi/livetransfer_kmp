//
//  LocationManager.swift
//  iosApp
//
//  Created by LeeJungHoon on 3/31/26.
//

import Foundation
import CoreLocation

/// CoreLocation을 이용해 현재 위치를 요청하고 결과를 @Published로 노출하는 ObservableObject.
/// - 정확도: kCLLocationAccuracyKilometer (날씨 조회 용도로 킬로미터 수준이면 충분)
/// - 권한 흐름: requestLocation() 호출 → 권한 미결정 시 팝업 → 허가되면 자동으로 위치 1회 조회
class LocationManager: NSObject, ObservableObject, CLLocationManagerDelegate{
    private let manager = CLLocationManager()

    /// 가장 최근에 수신된 위치. 아직 수신되지 않았으면 nil.
    @Published var location: CLLocation? = nil
    /// 현재 위치 권한 상태. UI에서 권한 안내 분기에 활용 가능.
    @Published var authorizationStatus: CLAuthorizationStatus = .notDetermined

    override init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyKilometer
    }

    /// 위치 권한을 요청하고 즉시 1회 위치를 조회한다.
    /// 권한이 .notDetermined 상태라면 시스템 팝업이 표시되며,
    /// 허가 후 locationManagerDidChangeAuthorization에서 자동으로 재요청된다.
    func requestLocation(){
        manager.requestWhenInUseAuthorization()
        manager.requestLocation()
    }

    // MARK: - CLLocationManagerDelegate

    func locationManager(_ manager:CLLocationManager, didUpdateLocations locations:[CLLocation]){
        location = locations.first
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
            print("위치 오류: \(error.localizedDescription)")
    }

    /// 권한 상태가 변경될 때 호출된다. 허가된 경우 즉시 위치를 요청한다.
    func locationManagerDidChangeAuthorization(_ manager:CLLocationManager){
        authorizationStatus = manager.authorizationStatus
        if(manager.authorizationStatus == .authorizedWhenInUse){
            manager.requestLocation()
        }
    }
}
