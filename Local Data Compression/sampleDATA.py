# -*- coding: utf-8 -*-
"""
Created on Tue Nov 05 18:27:36 2013

@author: puaykai
"""

from __future__ import division
import csv
import numpy as np

def sampleDATA():
    with open('C:\\Users\\puaykai\\Desktop\\UROP\\estimated49.csv','rb') as csvfile:
        areader = csv.reader(csvfile, delimiter=',')
    
        training = []
    
        for row in areader:
            training.append(map(float,row[2:]))
            
        training = training[1:]
        
        number_of_data = int(len(training))
        
        testing = np.array(training[int(number_of_data/2):number_of_data])
        [mean, sigma, training]= normalizeData(np.array(training[:int(number_of_data/2)]))
        
    return [training, testing]
        
def normalizeData(patches):
    
    mean = np.array([patches.mean(axis = 1)]).T
    
    patches = patches - mean
    
    sigma = patches.std()
    
    pstd = sigma *3
    
    patches = np.maximum(0-pstd, np.minimum(pstd, patches))/pstd
    
    patches = (patches + 1) * 0.4 + 0.1
    
    return [mean, sigma ,patches]
    
