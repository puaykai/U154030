# -*- coding: utf-8 -*-
"""
Created on Fri Jan 31 09:41:16 2014

@author: puaykai
"""
import multidimensional_LTC as ltc
from matplotlib.mlab import frange
import numpy as np
#testing - ( number_of_data, data_dimension)
#each station are treated as they are on the time line
def LTC_experiment(testing, epsilon, file_path):
    
    f_handle = file(file_path, 'a')
    
    #np.savetxt(file_path, ['compression ratio', 'errors'],delimiter = ",")
    
    print "Dealing with epsilon = ", epsilon
        
    result = [] # a list of rows with compression_ratio starting and a list of error vectors
        
    number_of_datas = np.shape(testing)[0]
        
    for data_index in range(number_of_datas):
            
        data = testing[:][data_index]
        
        ###################################
        #Converting the data into the (1, data[0]), (2, data[1]) ,... format
        formated_data = []
        for reading_index in range(len(data)):
                
            formated_data = formated_data + [((reading_index+1),np.array([data[reading_index]]))]
             
        #print "data: ", formated_data
        ######################################
        #Compression process
        
        formated_data.reverse()
                
        compressed_data = ltc.compress(formated_data, epsilon)
        
        compressed_data_len = len(compressed_data)
        
        compressed_data.reverse()
            
        uncompressed_data = [reading[0] for reading in ltc.decompress(compressed_data,1)]
            
        #print "uncompressed_data: ", np.array(uncompressed_data).shape
        #print "data: ", np.array(data).shape
        #######################################
        #Calculate error vector
            
        errors = (np.array(data) - np.array(uncompressed_data)).tolist()
        #print "errors",errors.shape
            
        #######################################
        #Calculate compression ratio 
        #print "compression_data_length: ", len(compressed_data)
        #print "compression_data_shape: ", compressed_data.shape
        #print "data_length: ", len(data)
        #print "data_shape: ", data.shape
        compression_ratio = (compressed_data_len+0.0)/len(data)
        
        #print "compression_ratio: ", compression_ratio
            
        ########################################
        #Append compression ratio to start of error vectors
            
        #print ([compression_ratio]+errors).shape
        
        #print np.sqrt(np.average(errors**2))
        
        #print errors
            
        row = [compression_ratio]+errors
        
        #print row
        
        written_row = [ str(entry)+"," for entry in row]
        
        #np.savetxt(f_handle, row, delimiter = ',')
        
        f_handle.write(''.join(written_row)+"\n")
            
        #result = result +[[compression_ratio] + errors]
        
    f_handle.close()
    #return result